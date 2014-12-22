package org.ops4j.pax.web.service.jetty.internal;

import java.util.Properties;

import org.ops4j.pax.web.service.jetty.CentralRequestHandler;
import org.ops4j.pax.web.service.jetty.CentralRequestHandlerAdmin;

import aQute.bnd.annotation.component.Component;

@Component(provide=CentralRequestHandlerAdmin.class)
public class CrhaImpl implements CentralRequestHandlerAdmin {

	@Override
	public void updateCentralHandlers(Properties rules) {
		JettyServerWrapper.instance.updateCentralHandlers(rules);		
	}

	@Override
	public CentralRequestHandler[] getCentralHandlers() {
		return JettyServerWrapper.instance.getCentralHandlers();
	}

	@Override
	public Properties getCentralHandlerProperties() {
		return JettyServerWrapper.instance.getCentralHandlerProperties();
	}

}
