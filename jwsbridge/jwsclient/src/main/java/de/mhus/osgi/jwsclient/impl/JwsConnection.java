package de.mhus.osgi.jwsclient.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import de.mhus.osgi.jwsclient.Connection;

public class JwsConnection extends Connection {

	
	private HashMap<String, JwsService> services = new HashMap<>();
	private URL url;

	public JwsConnection(JwsTarget jwsTarget) throws MalformedURLException {
		
		target = jwsTarget;
		
		url = new URL(target.getUrl());
		for (String service : target.getServices()) {
			JwsService s = new JwsService(this,service);
			services.put(service, s);
		}

	}

	public URL getUrl() {
		return url;
	}

	@Override
	public <T> T getService(String name, Class<? extends T> ifc) throws IOException {
		JwsService service = getService(name);
		if (service == null) throw new ServcieNotFoundException(name);
		return service.getService(ifc);
	}
	
	public JwsService getService(String name) {
		return services.get(name);
	}

	@Override
	public String[] getServiceNames() {
		return services.keySet().toArray(new String[1]);
	}
	
}
