package de.mhus.test.ws.ws_client.web;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import de.mhus.lib.core.logging.Log;
import de.mhus.test.ws.ws_model.WSService;

public class Connection {

	private static final Log log = Log.getLog(Connection.class);
	
	private WSService ws;
	private long updated;

	private Object session;

	public Connection(Object session) throws Exception {
		this.session = session;
		SessionUtil.setAttribute(session,"ws_connection__", this);
	}
	
	@SuppressWarnings("restriction")
	private synchronized void doInit() throws MalformedURLException {
		
		if (ws != null && updated == Singleton.get().getUpdated()) return;
		
		log.d("connect",session,Singleton.get().getUrlName(),Singleton.get().getNamespaceName(), Singleton.get().getServiceName());
		URL url = new URL(Singleton.get().getUrlName());
		QName qname = new QName(Singleton.get().getNamespaceName(), Singleton.get().getServiceName());
		
		Service service = Service.create(url, qname);
		ws = service.getPort(WSService.class);
		
		updated = Singleton.get().getUpdated();
		
	}

	public WSService getWSService() throws MalformedURLException {

		doInit();
		return ws;
	}

	// not synchronized !!!
	public void doResetConnection() {
		updated = 0;
	}

}
