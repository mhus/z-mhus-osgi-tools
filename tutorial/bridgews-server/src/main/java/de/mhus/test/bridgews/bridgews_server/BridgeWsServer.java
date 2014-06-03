package de.mhus.test.bridgews.bridgews_server;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.jws.WebService;

import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.jwsbridge.JavaWebService;
import de.mhus.osgi.jwsbridge.WebServiceInfo;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;

@WebService(endpointInterface = "de.mhus.test.ws.ws_model.WSService")
@Component(name="HohohoHeHeBridge",immediate=true,provide=JavaWebService.class)
public class BridgeWsServer implements JavaWebService, WSService {

	private Logger log = Logger.getLogger(BridgeWsServer.class.getName());
	private HashMap<String, WSEntity> map = new HashMap<String, WSEntity>();

	public BridgeWsServer() {
		addEntity(new WSEntity("alf")); // sample
	}
	
	public Object getServiceObject() {
		return this;
	}

	public String getServiceName() {
		return "itsabridge";
	}

	public void addEntity(WSEntity entity) {
		if (entity == null) return;
		log.info("add " + entity.getName());
		map.put(entity.getName(), entity);
	}

	public WSEntity[] getAll() {
		return map.values().toArray(new WSEntity[0]);
	}

	public void remvoeEntity(WSEntity entity) {
		if (entity == null) return;
		log.info("remove " + entity.getName());
		map.remove(entity.getName());
	}

	public void published(WebServiceInfo info) {
		
	}

	public void stopped(WebServiceInfo info) {
		
	}

}
