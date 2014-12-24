package de.mhus.osgi.web.virtualisation.api;

import java.util.logging.Logger;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualisation.api.central.CentralCallContext;

public interface VirtualHost {

	String CENTRAL_CONTEXT_KEY = "VirtualHost";

	Logger getLog();
	
	void setAttribute(String key, Object value);
	
	Object getAttribute(String key);
	
	boolean allowNativeRequest(String target);
	
	ResourceNode getResource(String target);
	
	VirtualApplication getApplication();
	
	void processError(CentralCallContext context);
	
}
