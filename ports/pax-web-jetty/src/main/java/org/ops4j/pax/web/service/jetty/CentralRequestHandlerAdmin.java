package org.ops4j.pax.web.service.jetty;

import java.util.Properties;

public interface CentralRequestHandlerAdmin {

	void updateCentralHandlers(Properties rules);
	CentralRequestHandler[] getCentralHandlers();
	Properties getCentralHandlerProperties();

}
