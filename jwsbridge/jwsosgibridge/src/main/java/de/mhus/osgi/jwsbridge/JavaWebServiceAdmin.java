package de.mhus.osgi.jwsbridge;

public interface JavaWebServiceAdmin {

	public static final String NAME = "de.mhus.osgi.jwsbridge.JavaWebServiceAdmin";

	WebServiceInfo[] getWebServices();
	void closeWebService(String name);
	void connect(String name);
	void disconnect(String name);
	
}
