package de.mhus.osgi.jwsclient;

import java.io.IOException;
import java.util.HashMap;

import de.mhus.osgi.jwsclient.impl.JwsFactory;

public class Client {

	protected HashMap<String, Target> targets = new HashMap<>();
	protected HashMap<String, TargetFactory> factories = new HashMap<>();
	{
		factories.put("jws", new JwsFactory());
	}
	
	public Target getTarget(String name) {
		synchronized (targets) {
			return targets.get(name);
		}
	}
	
	// Syntax: type|name|url|nameSpace|services (separated by comma)
	// Sample: jws|local|http://localhost:8181/cxf/hehe?wsdl|http://impl.ws_server.ws.test.mhus.de/|WSServiceImplService
	
	protected Target createTarget(String url) throws IOException {
		
		String[] parts = url.split("\\|");
		TargetFactory factory = getFactory(parts[0]);
		if (factory == null)
			throw new FactoryNotFoundException(parts[0], url);
		
		Target target = factory.createTarget(this, parts);
		synchronized (target) {
			targets.put(parts[1], target);
		}
		return target;
		
	}

	/**
	 * Return the named factory or null if not exists.
	 * 
	 * @param name
	 * @return
	 */
	protected TargetFactory getFactory(String name) {
		return factories.get(name);
	}
	
	protected void registerFactory(String name, TargetFactory factory) {
		factories.put(name, factory);
	}
	
	
}
