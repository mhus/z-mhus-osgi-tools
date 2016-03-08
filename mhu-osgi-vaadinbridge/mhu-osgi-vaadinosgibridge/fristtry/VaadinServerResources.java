package de.mhus.osgi.vaadinbridge.impl;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.vaadinbridge.VaadinResourceProvider;

@Component(name=VaadinServerResources.NAME,servicefactory=true)
public class VaadinServerResources implements VaadinResourceProvider {

	public static final String NAME = "com.vaadin.server";
	private Bundle targetBundle;
	private BundleContext context;
	
	@Override
	public boolean canHandle(String name) {
		if (name.equals("/vaadinBootstrap.js"))
			return true;
		return false;
	}

	@Override
	public URL getResource(String name) {
		
		if (name.equals("/vaadinBootstrap.js")) name = "/VAADIN" + name;
		
		Bundle bundle = getTargetBundle();
		if (bundle == null) {
			System.out.println("Bundle not found " + NAME);
			return null;
		}
		return bundle.getResource(name);
	}
	
	@Activate
	public void activate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
	}

	private Bundle getTargetBundle() {
		if (targetBundle != null && targetBundle.getState() != Bundle.UNINSTALLED) {
            return targetBundle;
        }
		for (Bundle b : context.getBundles()) {
			System.out.println("NAME: " + b.getSymbolicName());
			if (b.getSymbolicName().equals(NAME)) {
				targetBundle = b;
				break;
			}
		}
		
		return targetBundle;
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public long getLastModified(String name) {
		Bundle bundle = getTargetBundle();
		if (bundle == null) return -1;
		return bundle.getLastModified();
	}

}
