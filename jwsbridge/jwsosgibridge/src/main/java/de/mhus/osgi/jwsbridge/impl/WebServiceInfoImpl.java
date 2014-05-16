package de.mhus.osgi.jwsbridge.impl;

import javax.xml.ws.Endpoint;

import org.osgi.framework.ServiceReference;

import de.mhus.osgi.jwsbridge.JavaWebService;
import de.mhus.osgi.jwsbridge.WebServiceInfo;

public class WebServiceInfoImpl extends WebServiceInfo {

	private JavaWebServiceAdminImpl admin;
	private ServiceReference<JavaWebService> reference;
	private JavaWebService service;
	private Object webService;
	private String error;
	private Endpoint handler;
	private static long nextId = 0;
	private long id = ++nextId;

	public WebServiceInfoImpl(JavaWebServiceAdminImpl admin,
			ServiceReference<JavaWebService> reference) {
		this.admin = admin;
		this.reference = reference;
	}

	public void disconnect() {
		if (!isConnected()) return;
		handler.stop();handler = null;
	}

	public JavaWebService getJavaWebService() {
		return service;
	}

	public void connect() {
		if (isConnected()) return;
		error = null;
		handler = null;
		service = admin.getContext().getService(reference);
		webService = service.getServiceObject();
		setName(service.getServiceName());
		try {
			handler = Endpoint.publish("/" + getName(), webService);
		} catch (Throwable t) {
			error = t.getMessage();
			webService = null;
			handler = null;
		}
	}
	
	public boolean isConnected() {
		return handler != null;
	}
	
	
	public String getStatus() {
		if (error != null)
			return "Error: " + error;
		
		if (!isConnected())
			return "not connected";
		
		return handler.isPublished() ? "published" : "not published";
		
	}
	
	public String getBinding() {
		if (!isConnected()) return "";
		return handler.getBinding().getBindingID();
	}

	public boolean is(JavaWebService service2) {
		return service2.equals(service);
	}

	public boolean is(String name) {
		if (name.equals(String.valueOf(id))) return true;
		return name.equals(getName());
	}

	public long getId() {
		return id;
	}

	@Override
	public String getBundleName() {
		return reference.getBundle().getSymbolicName();
	}
	

}
