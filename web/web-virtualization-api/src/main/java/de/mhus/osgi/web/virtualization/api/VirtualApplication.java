package de.mhus.osgi.web.virtualization.api;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;

public interface VirtualApplication {

	String CENTRAL_CONTEXT_KEY = "VirtualApplication";

	boolean processRequest(VirtualHost host, CentralCallContext context);

	void configureHost(VirtualHost host, ResourceNode applicationConfig);

}
