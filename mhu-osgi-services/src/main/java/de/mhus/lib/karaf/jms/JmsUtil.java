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

import de.mhus.lib.errors.NotFoundRuntimeException;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.ServerService;

public class JmsUtil {

	public static JmsManagerService getService() {
		return JmsManagerServiceImpl.instance;
		// Using lookup will cause a lot off exceptions on startup
		/*
| 98 - mhu-lib-karaf - 3.3.2.SNAPSHOT | FrameworkEvent ERROR - mhu-lib-karaf
org.osgi.framework.ServiceException: Service factory returned null. (Component: JmsManagerService (824))
	at org.apache.felix.framework.ServiceRegistrationImpl.getFactoryUnchecked(ServiceRegistrationImpl.java:380)
	at org.apache.felix.framework.ServiceRegistrationImpl.getService(ServiceRegistrationImpl.java:247)
	at org.apache.felix.framework.ServiceRegistry.getService(ServiceRegistry.java:350)
	at org.apache.felix.framework.Felix.getService(Felix.java:3721)[org.apache.felix.framework-5.6.4.jar:]
	at org.apache.felix.framework.BundleContextImpl.getService(BundleContextImpl.java:470)[org.apache.felix.framework-5.6.4.jar:]
	at de.mhus.lib.mutable.KarafBase.lookup(KarafBase.java:55)
	at de.mhus.lib.core.lang.Base.lookup(Base.java:19)
	at de.mhus.lib.core.MApi.lookup(MApi.java:139)
	at de.mhus.lib.karaf.jms.JmsUtil.getService(JmsUtil.java:18)
	at de.mhus.lib.karaf.jms.JmsUtil.getConnection(JmsUtil.java:27)
	at de.mhus.lib.karaf.jms.AbstractJmsDataChannel.onConnect(AbstractJmsDataChannel.java:78)
	at de.mhus.lib.karaf.jms.JmsManagerServiceImpl.addChannel(JmsManagerServiceImpl.java:335)
	at de.mhus.lib.karaf.jms.JmsManagerServiceImpl$MyChannelTrackerCustomizer.addingService(JmsManagerServiceImpl.java:238)
	at de.mhus.lib.karaf.jms.JmsManagerServiceImpl$MyChannelTrackerCustomizer.addingService(JmsManagerServiceImpl.java:1)
	 */
		// return MApi.lookup(JmsManagerService.class);
	}
	
	/**
	 * Return the connection or null.
	 * @param name Name of the requested connection
	 * @return Connection
	 */
	public static JmsConnection getConnection(String name) {
		JmsManagerService service = getService();
		if (service == null) return null;
		return service.getConnection(name);
	}
	
	public static JmsDataChannel getChannel(String name) {
		JmsManagerService service = getService();
		if (service == null) return null;
		return service.getChannel(name);
	}
	
	public static <I> I getObjectForInterface(Class<? extends I> ifc) {
//		synchronized (channels) {
			
			{
				JmsDataChannel channel = getChannel(ifc.getCanonicalName());
				if (channel != null)
					try {
						@SuppressWarnings("unchecked")
						I o = ((ServerService<I>)channel.getChannel()).getObject();
						if (o != null) return o;
					} catch (Throwable t) {}
			}
			
			JmsManagerService service = getService();
			for (JmsDataChannel channel : service.getChannels()) {
				try {
					@SuppressWarnings("unchecked")
					I o = ((ServerService<I>)channel.getChannel()).getObject();
					if (o != null) return o;
				} catch (Throwable t) {}
			}
			throw new NotFoundRuntimeException("object for interface not found", ifc);
//		}
	}
}
