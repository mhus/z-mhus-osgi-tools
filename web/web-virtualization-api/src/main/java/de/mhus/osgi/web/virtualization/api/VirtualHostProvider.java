package de.mhus.osgi.web.virtualization.api;

public interface VirtualHostProvider {

	String CENTRAL_CONTEXT_KEY = "VirtualHostProvider";

	String[] getProvidedHosts();
	
	boolean existsHost(String host);
	
	VirtualHost getHost(String host);
	
}
