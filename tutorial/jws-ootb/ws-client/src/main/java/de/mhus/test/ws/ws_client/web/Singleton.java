package de.mhus.test.ws.ws_client.web;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import de.mhus.test.ws.ws_model.WSService;

public class Singleton {

	private static Singleton instance;
	
	private String urlName = "http://localhost:8181/cxf/hehe?wsdl";
	private String namespaceName = "http://impl.ws_server.ws.test.mhus.de/";
	private String serviceName = "WSServiceImplService";
	private long updated = System.currentTimeMillis();
	
	private Singleton() {}
	
	public static synchronized Singleton get() {
		if (instance == null)
			instance = new Singleton();
		return instance;
	}

	public String getUrlName() {
		return urlName;
	}

	public void setUrlName(String urlName) {
		this.urlName = urlName;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public Connection getConnection(Object session) throws Exception {
		
		synchronized (session) {
			Connection con = (Connection) SessionUtil.getAttribute(session, "ws_connection__");
			if (con == null) {
				con = new Connection(session);
			}
			return con;
		}
		
	}

	public long getUpdated() {
		return updated;
	}

	public void reset() {
		this.updated = System.currentTimeMillis();
	}
	
}
