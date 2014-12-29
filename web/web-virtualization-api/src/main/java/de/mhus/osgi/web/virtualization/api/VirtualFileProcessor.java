package de.mhus.osgi.web.virtualization.api;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;

public interface VirtualFileProcessor {

	boolean processRequest(VirtualHost host, ResourceNode res, CentralCallContext context) throws Exception;

	ProcessorMatcher getDefaultMatcher();
	
}
