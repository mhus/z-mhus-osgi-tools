package de.mhus.osgi.portletosgi;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;

import javax.portlet.Portlet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MXml;

@Component(name="PortletManager",immediate=true, provide=PortletManager.class)
public class PortletManagerImpl extends MLog implements PortletManager, BundleListener {

	private BundleContext context;
	private ServiceTracker<PortletFactory, PortletFactory> tracker;
	private HashMap<String, PortletFactory> factories;

	@Activate
	public void doActivate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
		factories = new HashMap<String, PortletFactory>();
		tracker = new ServiceTracker<PortletFactory,PortletFactory>(context, PortletFactory.class, new PFCustomizer() );
		tracker.open();
		context.addBundleListener(this);
		refreshAll();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		tracker.close();
		context.removeBundleListener(this);
		factories = null;
	}

	private class PFCustomizer implements ServiceTrackerCustomizer<PortletFactory, PortletFactory> {

		@Override
		public de.mhus.osgi.portletosgi.PortletFactory addingService(
				ServiceReference<de.mhus.osgi.portletosgi.PortletFactory> reference) {
			PortletFactory inst = context.getService(reference);
			addFactory(inst);
			return inst;
		}

		@Override
		public void modifiedService(ServiceReference<de.mhus.osgi.portletosgi.PortletFactory> reference,
				de.mhus.osgi.portletosgi.PortletFactory service) {
			addFactory(service);
		}

		@Override
		public void removedService(ServiceReference<de.mhus.osgi.portletosgi.PortletFactory> reference,
				de.mhus.osgi.portletosgi.PortletFactory service) {
			removeFactory(service.getName());
		}
		
	}

	public void addFactory(PortletFactory inst) {
		if (inst == null || !inst.isValid()) return;
		String name = inst.getName();
		log().d("add factory", name);
		synchronized(factories) {
			if (factories.put(name, inst) != null)
				log().i("overwrite portlet",name);
		}
	}

	public void removeFactory(String name) {
		log().d("remove factory", name);
		synchronized(factories) {
			factories.remove(name);
		}
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (factories == null) return;
		if (event.getType() == BundleEvent.STARTED) {
			doBundle(event.getBundle());
		}
	}

	public void doBundle(Bundle bundle) {
		
		try {
			log().d("Bundle: " + bundle.getSymbolicName());
			URL portletDef = bundle.getEntry("/WEB-INF/portlet.xml");
			if (portletDef == null) return;
			Document def = MXml.loadXml(portletDef.openStream());
			doReadPortletDef(def.getDocumentElement());
		} catch (Throwable t) {
			log().e(t);
		}
	}

	private void doReadPortletDef(Element elem) {
		for (Element portletElem : MXml.getLocalElementIterator(elem, "portlet")) {
			doReadPortlet(portletElem);
		}
	}

	private void doReadPortlet(Element elem) {
		WarPortletFactory factory = new WarPortletFactory(elem);
		addFactory(factory);
	}

	@Override
	public void refreshAll() {
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getState() == Bundle.ACTIVE) {
				doBundle(bundle);
			}
		}
	}

}
