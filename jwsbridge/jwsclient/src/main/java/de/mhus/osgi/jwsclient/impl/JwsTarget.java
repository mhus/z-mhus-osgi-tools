package de.mhus.osgi.jwsclient.impl;

import java.io.IOException;

import de.mhus.osgi.jwsclient.Connection;
import de.mhus.osgi.jwsclient.Client;
import de.mhus.osgi.jwsclient.Target;

public class JwsTarget extends Target {


	private String nameSpace;

	public JwsTarget(JwsFactory jwsFactory, Client jwsClient, String[] parts) {
		client = jwsClient;
		factory = jwsFactory;
		url = parts[2];
		nameSpace = parts[3];
		
		services = parts[4].split(",");
	}

	@Override
	public Connection createConnection() throws IOException {
		return new JwsConnection(this);
	}

	public String getUrl() {
		return url;
	}
	
	public String getNameSpace() {
		return nameSpace;
	}
		
}
