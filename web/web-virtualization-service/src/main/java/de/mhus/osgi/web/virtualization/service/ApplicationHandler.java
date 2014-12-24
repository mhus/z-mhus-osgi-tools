package de.mhus.osgi.web.virtualization.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.web.virtualization.api.VirtualApplication;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.VirtualHostProvider;
import de.mhus.osgi.web.virtualization.api.central.AbstractCentralRequestHandler;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.api.central.CentralRequestHandler;
import de.mhus.osgi.web.virtualization.api.util.ExtendedServletResponse;

@Component(immediate=true,provide=CentralRequestHandler.class,name="ApplicationHandler")
public class ApplicationHandler extends AbstractCentralRequestHandler {
	
	@Override
	public boolean doHandleBefore(CentralCallContext context)
			throws IOException, ServletException {
				
		VirtualHost vh = (VirtualHost) context.getAttribute(VirtualHost.CENTRAL_CONTEXT_KEY);
		if (vh != null) {
			VirtualApplication app = vh.getApplication();
			context.setAttribute(VirtualApplication.CENTRAL_CONTEXT_KEY, app);
			if (app != null) {
				return app.process(vh, context);
			}
		}
		return false;
	}

	@Override
	public void doHandleAfter(CentralCallContext context) throws IOException,
			ServletException {
	}
	
	@Override
	public double getSortHint() {
		return 0;
	}

	@Override
	public void configure(Properties rules) {
		
	}

}
