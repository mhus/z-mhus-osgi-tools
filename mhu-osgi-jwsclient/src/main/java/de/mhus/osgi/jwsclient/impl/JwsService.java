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
package de.mhus.osgi.jwsclient.impl;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;


public class JwsService {

	private String name;
	private JwsConnection connection;
	private QName qname;
	private Service service;
	private Object port;

	public JwsService(JwsConnection jwsConnection, String serviceName) {
		name = serviceName;
		connection = jwsConnection;
				
		qname = new QName(((JwsTarget)connection.getTarget()).getNameSpace(), name);
		service = Service.create(connection.getUrl(), qname);
		
	}
	
	protected <T> T createService(Class<? extends T> ifc) {
		return service.getPort(ifc);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<? extends T> ifc) {
		synchronized (this) {
			if (port == null) {
				port = createService(ifc);
			}
			return (T)port;
		}
	}

	public String getName() {
		return name;
	}

}
