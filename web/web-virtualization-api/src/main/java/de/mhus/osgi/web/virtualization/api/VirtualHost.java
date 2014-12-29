package de.mhus.osgi.web.virtualization.api;

import java.util.logging.Logger;

import javax.servlet.ServletContext;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.lib.core.logging.Log;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;

public interface VirtualHost {

	String CENTRAL_CONTEXT_KEY = "VirtualHost";

	Log getLog();
	
	void setAttribute(String key, Object value);
	
	Object getAttribute(String key);
	
	boolean allowNativeRequest(String target);
	
	ResourceNode getResource(String target);
	
	boolean processRequest(CentralCallContext context) throws Exception;
	
	void processError(CentralCallContext context);
	
	ClassLoader getHostClassLoader();
		
}
