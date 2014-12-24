package de.mhus.osgi.web.virtualisation.api;

public interface VirtualHostProvider {

	String[] getProvidedHosts();
	
	boolean existsHost(String host);
	
	VirtualHost getHost(String host);
	
}
