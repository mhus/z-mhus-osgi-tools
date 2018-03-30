/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		
		synchronized (targets) {
			Target target = targets.get(parts[1]);
			if (target != null) return target;
			
			target = factory.createTarget(this, parts);
			targets.put(parts[1], target);
			return target;
		}
		
	}

	public void closeTarget(String targetName) {
		synchronized (targets) {
			targets.remove(targetName);
		}
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
