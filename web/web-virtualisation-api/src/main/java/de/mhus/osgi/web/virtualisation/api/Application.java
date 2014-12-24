package de.mhus.osgi.web.virtualisation.api;

import de.mhus.osgi.web.virtualisation.api.central.CentralCallContext;

public interface Application {

	void process(VirtualHost host, CentralCallContext context);

}
