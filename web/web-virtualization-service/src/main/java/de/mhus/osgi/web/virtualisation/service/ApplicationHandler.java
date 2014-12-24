package de.mhus.osgi.web.virtualisation.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import de.mhus.osgi.web.virtualisation.api.VirtualApplication;
import de.mhus.osgi.web.virtualisation.api.VirtualHost;
import de.mhus.osgi.web.virtualisation.api.VirtualHostProvider;
import de.mhus.osgi.web.virtualisation.api.central.AbstractCentralRequestHandler;
import de.mhus.osgi.web.virtualisation.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualisation.api.util.ExtendedServletResponse;

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
