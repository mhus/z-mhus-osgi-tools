package de.mhus.osgi.web.virtualisation.api;

public interface VirtualHostProvider {

	boolean existsVirtualHost(String host);
	
	VirtualHost getVirtualHost(String host);
	
}
