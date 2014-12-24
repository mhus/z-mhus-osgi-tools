package de.mhus.osgi.web.virtualisation.api.central;

import java.util.Properties;

public interface CentralRequestHandlerAdmin {

	void updateCentralHandlers(Properties rules);
	CentralRequestHandler[] getCentralHandlers();
	Properties getCentralHandlerProperties();

}
