package de.mhus.osgi.vaadinbridge;


public interface VaadinResourceProvider {
	
	boolean canHandle(String name);
	
	Resource getResource(String name);
	
	String getName();

	long getLastModified(String name);
	
}
