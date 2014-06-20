package de.mhus.osgi.jwsbridge;

import javax.xml.ws.Endpoint;

public abstract class WebServiceInfo {

	private String name;

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}
	
	public abstract boolean isConnected();
	
	public abstract String getStatus();

	public abstract long getId();

	public abstract String getBundleName();

	public abstract String getBindingInfo();

	public abstract Endpoint getEndpoint();
	

}
