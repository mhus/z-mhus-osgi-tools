package de.mhus.osgi.web.virtualisation.api;

import java.util.logging.Logger;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualisation.api.central.CentralCallContext;

public interface VirtualHost {

	Logger getLog();
	
	void setAttribute(String key, Object value);
	
	Object getAttribute(String key);
	
	boolean allowNativeRequest(String target);
	
	ResourceNode getResource(String target);
	
	Application getApplication();
	
	void processError(CentralCallContext context);
	
}
