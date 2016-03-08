package de.mhus.osgi.jwsbridge;

public interface JavaWebService {

	Object getServiceObject();

	String getServiceName();
	
	void published(WebServiceInfo info);
	
	void stopped(WebServiceInfo info);
	
}
