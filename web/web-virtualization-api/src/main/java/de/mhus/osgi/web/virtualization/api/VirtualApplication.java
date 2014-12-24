package de.mhus.osgi.web.virtualization.api;

import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;

public interface VirtualApplication {

	String CENTRAL_CONTEXT_KEY = "VirtualApplication";

	boolean process(VirtualHost host, CentralCallContext context);

}
