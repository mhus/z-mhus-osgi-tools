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
package de.mhus.osgi.jwsbridge.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.osgi.framework.ServiceReference;

import de.mhus.osgi.jwsbridge.JavaWebService;
import de.mhus.osgi.jwsbridge.WebServiceInfo;

public class WebServiceInfoImpl extends WebServiceInfo {

	private static Logger log = Logger.getLogger(WebServiceInfoImpl.class.getSimpleName());
	@SuppressWarnings("unused")
	private JavaWebServiceAdminImpl admin;
	private ServiceReference<JavaWebService> reference;
	private JavaWebService service;
	private Object webService;
	private String error;
	private Endpoint endpoint;
	private static long nextId = 0;
	private long id = ++nextId;

	public WebServiceInfoImpl(JavaWebServiceAdminImpl admin,
			ServiceReference<JavaWebService> reference) {
		this.admin = admin;
		this.reference = reference;
		service = admin.getContext().getService(reference);
		if (service != null) setName(service.getServiceName());
	}

	public void disconnect() {
		if (!isConnected()) return;
		log.fine("JWS Disconnect: " + getName());
		endpoint.stop();
		webService = null;
		service.stopped(this);
		endpoint = null;
		
	}

	public JavaWebService getJavaWebService() {
		return service;
	}

	public void connect() {
		if (isConnected() || getName() == null || getName().length() == 0 || service == null) return;
		error = null;
		endpoint = null;
		webService = service.getServiceObject();
		try {
			log.fine("JWS Connect: " + getName());
			endpoint = Endpoint.publish("/" + getName(), webService);
		} catch (Throwable t) {
			error = t.getMessage();
			webService = null;
			endpoint = null;
			log.log(Level.WARNING, "ERROR: " + getName() + " " + webService, t);
		}
		if (endpoint != null)
			service.published(this);
	}
	
	@Override
	public boolean isConnected() {
		return endpoint != null;
	}
	
	
	@Override
	public String getStatus() {
		if (error != null)
			return "Error: " + error;
		
		if (!isConnected())
			return "not connected";
		
		return endpoint.isPublished() ? "published" : "not published";
		
	}
	
	@Override
	public String getBindingInfo() {
		if (!isConnected()) return "";
		
		try {
			//TODO This could be more generic
			String myName = webService.getClass().getSimpleName();
			if (myName.endsWith("Impl")) myName = myName.substring(0, myName.length()-4);
			return "jws|" + myName + "|http://localhost:8181/cxf/" + getName() + "?wsdl";
		} catch (Throwable t) {}
		return "/" + getName();
	}

	public boolean is(JavaWebService service2) {
		return service2.equals(service);
	}

	public boolean is(String name) {
		if (name.equals(String.valueOf(id))) return true;
		return name.equals(getName());
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getBundleName() {
		return reference.getBundle().getSymbolicName();
	}
	
	@Override
	public Endpoint getEndpoint() {
		return endpoint;
	}
	
	@Override
	public void setName(String name) {
		if (name == null || name.length() == 0 || name.equals(getName())) return;
		boolean connected = isConnected();
		disconnect();
		super.setName(name);
		if (connected) connect();
	}

}
