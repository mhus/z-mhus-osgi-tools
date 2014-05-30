package de.mhus.osgi.jwsclient;

import java.io.IOException;

public abstract class Target {

	protected Client client;
	protected TargetFactory factory;
	protected String url;
	protected String[] services;

	public Client getClient() {
		return client;
	}
	
	public TargetFactory getFactory() {
		return factory;
	}
	
	public abstract Connection createConnection() throws IOException;

	public String getUrl() {
		return url;
	}
	
	// Insecure: caller can manipulate values of the array
	public String[] getServices() {
		return services;
	}
	
}
