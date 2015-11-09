package de.mhus.osgi.vaadinbridge.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.osgi.vaadinbridge.BundleWatch;
import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

/*
vaadinBootstrap.js

com.vaadin.client-compiled: /widgetsets/com.vaadin.DefaultWidgetSet
com.vaadin.server: /vaadinBootstrap.js
com.vaadin.themes: /themes/reindeer,/themes/runo,/themes/liferay,/themes/chameleon,/themes/base

 */
@Component(provide=BundleWatch.class,name="VaadinBridgeBundleWatch",immediate=true)
public class BundleWatchImpl implements BundleWatch, BundleListener, ServiceListener {
	
	private static final Logger log = Logger.getLogger(BundleWatchImpl.class.getName());
	private BundleContext context;
	private boolean enabled = true;
	private ServiceRegistration<?> configUpdaterReg;
	private static final String CONFIG_PID = "de.mhus.osgi.vaadinbridge";

	@Activate
	public void doActivate(ComponentContext ctx) {
		log.info("Start");
		context = ctx.getBundleContext();
		context.addBundleListener(this);
		context.addServiceListener(this);
		
		// configuration
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, CONFIG_PID);
		configUpdaterReg = context.registerService(ManagedService.class.getName(), new ConfigUpdater(this) , properties);
		
		if (enabled) refreshAll();
		
		
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		log.info("Stop");
		configUpdaterReg.unregister();
		context.removeBundleListener(this);
		context.removeServiceListener(this);
		
	}

	@Override
	public void bundleChanged(BundleEvent event) {

		if (!enabled) return;

		if (event.getType() == BundleEvent.STARTED) {
			doBundle(event.getBundle());
		}
	}
	
	public void doBundle(Bundle bundle) {
				
		log.fine("Bundle: " + bundle.getSymbolicName());
		Enumeration<String> list = bundle.getEntryPaths("/VAADIN");
		if (list == null) return;
		
		ServiceReference<VaadinConfigurableResourceProviderAdmin> sr = (ServiceReference<VaadinConfigurableResourceProviderAdmin>) context.getServiceReference(VaadinConfigurableResourceProviderAdmin.class.getName());
		if (sr == null) return;
		VaadinConfigurableResourceProviderAdmin admin = context.getService(sr);
		if (admin == null) return;

		
		LinkedList<String> resources = new LinkedList<String>();
		while (list.hasMoreElements()) {
			String path = list.nextElement();
//			log.info(bundle.getSymbolicName() + ": " + path );
			path = path.substring("VAADIN".length());			
			
			if (path != null && 
				bundle.getSymbolicName() != null &&
				bundle.getSymbolicName().equals("com.vaadin.server") && 
				path.equals("/vaadinBootstrap.js"))
			{
				resources.add(path);
			} else
			if (path != null && path.indexOf("gwt-unitCache") > -1) {
				// ignore
			} else {
				Enumeration<String> list2 = bundle.getEntryPaths("/VAADIN" + path);
				if (list2 != null) {
					while (list2.hasMoreElements()) {
						String path2 = list2.nextElement();
						if ( path2.indexOf("WEB-INF") > -1 ) {
							// ignore
						} else {
							path2 = path2.substring("VAADIN".length());
							if (path2.endsWith("/")) path2 = path2.substring(0, path2.length()-1);
							resources.add(path2);
						}
					}
				}
			}
			
		}
		
		log.info(bundle.getSymbolicName() + ": " + resources );
		if (resources.size() > 0) {
			String[] current = admin.getResourcePathes(bundle.getSymbolicName());
			if (current != null) {
				admin.removeResource(bundle.getSymbolicName());
			}
			try {
				admin.addResource(bundle.getSymbolicName() == null ? ""+bundle.getBundleId() : bundle.getSymbolicName(), resources.toArray(new String[resources.size()]));
			} catch (Throwable t) {
				log.warning("can't add resources of bundle " + bundle + " " + t);
			}
		}
	}

	@Override
	public void refreshAll() {
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getState() == Bundle.ACTIVE) {
				try {
					doBundle(bundle);
				} catch (Throwable t) {
					log.warning("can't refresh bundle " + bundle + " " + t);
				}
			}
		}
	}

	@Override
	public void serviceChanged(ServiceEvent event) {

		if (!enabled) return;

		try {
			if (event.getType() != ServiceEvent.REGISTERED) return;
			Object service = context.getService(event.getServiceReference());
			if (service == null) return;
			if (!(service instanceof VaadinConfigurableResourceProviderAdmin)) return;
	
			log.info("Admin-Service registered");
			refreshAll();
		} catch (Throwable t) {
			t.printStackTrace(); //TODO log
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		boolean old = this.enabled;
		this.enabled = enabled;
		if (!old && enabled)
			refreshAll();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
}
