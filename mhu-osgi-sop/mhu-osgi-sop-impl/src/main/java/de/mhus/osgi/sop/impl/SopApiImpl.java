package de.mhus.osgi.sop.impl;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgProperties;
import de.mhus.osgi.sop.api.SopApi;

@Component(immediate=true,provide=SopApi.class,name="SopApi")
public class SopApiImpl extends MLog implements SopApi {
	

	@SuppressWarnings("unused")
	private BundleContext context;

	private CfgProperties config = new CfgProperties(SopApi.class, "sop");
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		// TODO set synchronizer
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		context = null;
	}
	
	@Override
	public IProperties getMainConfiguration() {
		return config.value();
	}
	
}