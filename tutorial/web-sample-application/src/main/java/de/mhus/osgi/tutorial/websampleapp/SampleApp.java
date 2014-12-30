package de.mhus.osgi.tutorial.websampleapp;

import de.mhus.osgi.web.virtualization.api.VirtualApplication;
import de.mhus.osgi.web.virtualization.impl.osgi.AbstractOsgiBundleApplication;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component(provide=VirtualApplication.class,immediate=true,properties="name=sample",name="SampleApp")
public class SampleApp extends AbstractOsgiBundleApplication {

	@Activate
	public void doActivate(ComponentContext ctx) {
		super.doActivate(ctx);
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		super.doDeactivate(ctx);
	}

}
