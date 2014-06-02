package de.mhus.osgi.jwsbridge.impl;

import javax.xml.ws.Endpoint;

import org.osgi.framework.ServiceReference;

import de.mhus.osgi.jwsbridge.JavaWebService;
import de.mhus.osgi.jwsbridge.WebServiceInfo;

import org.apache.cxf.jaxws.EndpointImpl;

public class WebServiceInfoImpl extends WebServiceInfo {

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
			endpoint = Endpoint.publish("/" + getName(), webService);
		} catch (Throwable t) {
			error = t.getMessage();
			webService = null;
			endpoint = null;
		}
		if (endpoint != null)
			service.published(this);
	}
	
	public boolean isConnected() {
		return endpoint != null;
	}
	
	
	public String getStatus() {
		if (error != null)
			return "Error: " + error;
		
		if (!isConnected())
			return "not connected";
		
		return endpoint.isPublished() ? "published" : "not published";
		
	}
	
	public String getBindingInfo() {
		if (!isConnected()) return "";
		
		try {
			return "jws|" + getName() + "|http://localhost:8080/cxf/" + getName() + "?wsdl|http://" + turnArround(webService.getClass().getPackage().getName()) + "/|" + webService.getClass().getSimpleName() + "Service";
		} catch (Throwable t) {}
		return "/" + getName();
	}

	private String turnArround(String name) {
		String[] parts = name.split("\\.");
		StringBuffer out = new StringBuffer();
		for (String part : parts) {
			if (out.length() != 0)
				out.insert(0, ".");
			out.insert(0, part);
		}
		return out.toString();
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
	
	public Endpoint getEndpoint() {
		return endpoint;
	}
	
	public void setName(String name) {
		if (name == null || name.length() == 0 || name.equals(getName())) return;
		boolean connected = isConnected();
		disconnect();
		super.setName(name);
		if (connected) connect();
	}

}
