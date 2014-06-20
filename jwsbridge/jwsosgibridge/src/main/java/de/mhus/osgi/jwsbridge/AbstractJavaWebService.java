package de.mhus.osgi.jwsbridge;

public abstract class AbstractJavaWebService implements JavaWebService {

	@Override
	public String getServiceName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void published(WebServiceInfo info) {
		
	}

	@Override
	public void stopped(WebServiceInfo info) {
		
	}
	
	@Override
	public Object getServiceObject() {
		return this;
	}

}
