package de.mhus.osgi.web.virtualization.api.central;

import java.util.Properties;

public interface CentralRequestHandlerAdmin {

	void updateCentralHandlers(Properties rules);
	CentralRequestHandler[] getCentralHandlers();
	Properties getCentralHandlerProperties();

}
