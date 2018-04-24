/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.lib.karaf.jms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import javax.jms.JMSException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MTimerTask;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.lib.karaf.MOsgi.Service;

/**
 * Note: because of a 'new behavior' or bug in Felix we need to delay the start of the
 * service trackers. Immediately will cause an 'Circular reference detected' exception.
 * 
 * Discussion: https://github.com/eclipse/smarthome/issues/870
 * 
 * Switching to DS driven references was also not successful, got the same exception.
 * 
 * You can chnage the behavior in mhus-config by setting JmsManagerService/startupDelay to zero
 * 
 * @author mikehummel
 *
 */
@Component(name="JmsManagerService",immediate=true)
public class JmsManagerServiceImpl extends MLog implements JmsManagerService {

	static JmsManagerService instance;
	private ServiceTracker<JmsDataSource, JmsDataSource> connectionTracker;
	private ServiceTracker<JmsDataChannel, JmsDataChannel> channelTracker;
	
	private WeakHashMap<String, JmsDataChannel> channels = new WeakHashMap<>();
	private HashSet<String> connectionNames = new HashSet<>();
	
	private BundleContext context;
	private TimerIfc timer;
	private MTimerTask timerTask;
	protected boolean enabled = true;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		
		log().i("activate");
		instance = this;
		context = ctx.getBundleContext();
		connectionTracker = new ServiceTracker<>(context, JmsDataSource.class, new MyConnectionTrackerCustomizer() );
		connectionTracker.open();

		channelTracker = new ServiceTracker<>(context, JmsDataChannel.class, new MyChannelTrackerCustomizer() );
		channelTracker.open();
		
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		
		if (timer != null)
			timer.cancel();
		if (channelTracker != null) channelTracker.close();
		if (connectionTracker != null) connectionTracker.close();
		for ( String name : new LinkedList<String>(channels.keySet()))
			removeChannel(name);
		
		instance = null;

	}

	@Reference(service=TimerFactory.class)
	public void setTimerFactory(TimerFactory factory) {
		log().i("create timer");
		timer = factory.getTimer();
		timerTask = new MTimerTask() {
			
			@Override
			public void doit() throws Exception {
				if (!enabled ) return;
				// check connections and channels
				doBeat();
				// reconnect
				doChannelBeat();
			}
		};
		timer.schedule(timerTask, 60000, 60000);
	}

	@Override
	public void doBeat() {
		// check connections
		for ( JmsDataSource con : MOsgi.getServices(JmsDataSource.class, null)) {
			String name = con.getName();
			if (name == null) continue;
			if (!connectionNames.contains(name)) {
				try {
					addConnection(name, con.getConnection());
				} catch (Throwable e) {
					log().d(name,e);
				}
			}
		}
		
		// check the channels
		for (JmsDataChannel c : MOsgi.getServices(JmsDataChannel.class, null)) {
			String name = c.getName();
			if (name == null) continue;
			if (!channels.containsKey(name)) {
				try {
					addChannel(c);
				} catch (Throwable e) {
					log().d(name,e);
				}
			}
		}
		
	}

	@Override
	public List<JmsConnection> getConnections() {
		LinkedList<JmsConnection> out = new LinkedList<>();
		for (JmsDataSource obj : MOsgi.getServices(JmsDataSource.class, null))
			try {
				out.add(obj.getConnection());
			} catch (JMSException e) {
				log().w(e);
			}
		return out;
	}

	@Override
	public List<MOsgi.Service<JmsDataSource>> getDataSources() {
		LinkedList<MOsgi.Service<JmsDataSource>> out = new LinkedList<>();
		for (MOsgi.Service<JmsDataSource> obj : MOsgi.getServiceRefs(JmsDataSource.class, null))
			out.add(obj);
		return out;
	}
	
	@Override
	public JmsConnection getConnection(String name) {
		if (name == null) return null;
		try {
			JmsDataSource src = MOsgi.getService(JmsDataSource.class,"(osgi.jndi.service.name=jms_" + name + ")");
			if (src == null) return null;
			return src.getConnection();
		} catch (NotFoundException nfe) { 
			for (Service<JmsDataSource> c : getDataSources())
				if (c.getService() != null && name.equals(c.getService().getName()))
					try {
						return c.getService().getConnection();
					} catch (JMSException e) {
						log().w(name,e);
					}
			return null;
		} catch (JMSException e) {
			log().w(name,e);
			return null;
		}
	}
	
	@Override
	public void resetChannels() {
		synchronized (channels) {
			List<JmsDataChannel> channels = getChannels();
			for (JmsDataChannel channel : channels)
				try {
					channel.reset();
				} catch (Throwable t) {
					log().t(channel.getName(),t);
				}
		}
	}

	private class MyConnectionTrackerCustomizer implements ServiceTrackerCustomizer<JmsDataSource, JmsDataSource> {

		@Override
		public JmsDataSource addingService(
				ServiceReference<JmsDataSource> reference) {
			
			JmsDataSource service = context.getService(reference);
			
			try {
				addConnection(service.getName(), service.createConnection());
			} catch (JMSException e) {
				log().t(e);
			}
			resetChannels();
			
			return service;
		}

		@Override
		public void modifiedService(ServiceReference<JmsDataSource> reference,
				JmsDataSource service) {
			
			removeConnection(service.getName());
			try {
				addConnection(service.getName(), service.createConnection());
			} catch (JMSException e) {
				log().t(e);
			}
			resetChannels();
			
		}

		@Override
		public void removedService(ServiceReference<JmsDataSource> reference,
				JmsDataSource service) {
			removeConnection(service.getName());
			resetChannels();
		}
		
	}
	
	private class MyChannelTrackerCustomizer implements ServiceTrackerCustomizer<JmsDataChannel, JmsDataChannel> {

		@Override
		public JmsDataChannel addingService(
				ServiceReference<JmsDataChannel> reference) {

			try {
				JmsDataChannel service = context.getService(reference);
				if (service != null)
					addChannel(service);
				return service;
			} catch (Throwable t) {
				log().e(reference,t);
				return null;
			}
		}

		@Override
		public void modifiedService(ServiceReference<JmsDataChannel> reference,
				JmsDataChannel service) {

			removeChannel(service.getName());
			addChannel(service);
		}

		@Override
		public void removedService(ServiceReference<JmsDataChannel> reference,
				JmsDataChannel service) {
			removeChannel(service.getName());
		}
		
	}

	@Override
	public JmsDataChannel getChannel(String name) {
		try {
			return MOsgi.getService(JmsDataChannel.class,"(osgi.jndi.service.name=jmschannel_" + name + ")");
		} catch (NotFoundException nfe) {
			for (JmsDataChannel c : getChannels())
				if (name.equals(c.getName()))
					return c;
			return channels.get(name);
		}
	}

	public void removeConnection(String name) {
		if (name == null) return;
		log().d("remove connection",name);
		connectionNames.remove(name);
		for ( JmsDataChannel c : new LinkedList<JmsDataChannel>(channels.values()))
			if (name.equals(c.getConnectionName()))
				try {
					c.onDisconnect();
				} catch (Throwable t) {
					log().w(name,c,t);
				}
	}

	public void addConnection(String name, JmsConnection connection) {
		if (name == null) return;
		log().d("add connection",name);
		connectionNames.add(name);
		for ( JmsDataChannel c : new LinkedList<JmsDataChannel>(channels.values()))
			if (name.equals(c.getConnectionName()))
				try {
					c.onConnect();
				} catch (Throwable t) {
					log().w(name,c,t);
				}
	}

	@Override
	public List<JmsDataChannel> getChannels() {
		LinkedList<JmsDataChannel> out = new LinkedList<>();
		out.addAll(channels.values());
//		for (JmsDataChannel obj : MOsgi.getServices(JmsDataChannel.class, null))
//			out.add(obj);
//		out.addAll(channels.values());
		return out;
	}
	
	@Override
	public void doChannelBeat() {
		synchronized (channels) {
			
			for ( JmsDataChannel c : new LinkedList<JmsDataChannel>(channels.values()))
				try {
					c.doBeat();
				} catch (Throwable t) {
					log().d(c,t);
				}
		}
	}

	@Override
	public String getServiceName(Service<JmsDataSource> ref) {
		Object p = ref.getReference().getProperty("osgi.jndi.service.name");
		if (p != null && p instanceof String && ((String)p).length() > 4 && ((String)p).startsWith("jms_"))
			return ((String)p).substring(4);
		return null;
	}

	@Override
	public void addChannel(JmsDataChannel channel) {
		log().d("add channel",channel.getName());
		channels.put(channel.getName(), channel);
		channel.onConnect();
	}

	@Override
	public void removeChannel(String name) {
		log().d("remove channel",name);
		JmsDataChannel c = channels.remove(name);
		if (c != null)
			c.onDisconnect();
	}

	@Override
	public void resetConnection(String name) {
		if (name == null) return;
		log().d("reset connection",name);
		for ( JmsDataChannel c : new LinkedList<JmsDataChannel>(channels.values()))
			if (name.equals(c.getConnectionName()))
				try {
					c.onDisconnect();
				} catch (Throwable t) {
					log().w(name,c,t);
				}
	}
	
}
