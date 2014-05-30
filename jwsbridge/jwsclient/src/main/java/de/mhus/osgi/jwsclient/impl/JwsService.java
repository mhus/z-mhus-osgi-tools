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
