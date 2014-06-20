package de.mhus.test.bridgews.bridgews_token_server;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.jws.WebService;

import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.jwsbridge.JavaWebService;
import de.mhus.osgi.jwsbridge.WebServiceInfo;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.EndpointImpl;

@WebService(endpointInterface = "de.mhus.test.ws.ws_model.WSService")
@Component(name="securetoken",immediate=true,provide=JavaWebService.class)
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
		return "itsabridgewithtoken";
	}

	public void addEntity(WSEntity entity) {
		if (entity == null) return;
		log.info("add " + entity.getName());
		map.put(entity.getName(), entity);
	}

	public WSEntity[] getAll() {
		System.out.println("getAll Token: " + TokenInInterceptor.currentToken.get());
		return map.values().toArray(new WSEntity[0]);
	}

	public void removeEntity(WSEntity entity) {
		if (entity == null) return;
		log.info("remove " + entity.getName());
		map.remove(entity.getName());
	}

	public void published(WebServiceInfo info) {
		EndpointImpl jaxWsEndpoint = (EndpointImpl) info.getEndpoint();
		Endpoint cxfEndpoint = jaxWsEndpoint.getServer().getEndpoint();
		
		
//		Map<String,Object> outProps = new HashMap<String,Object>();
//		WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
//		cxfEndpoint.getOutInterceptors().add(wssOut);

		TokenInInterceptor wssIn = new TokenInInterceptor();
		cxfEndpoint.getInInterceptors().add(wssIn);
		
	}

	public void stopped(WebServiceInfo info) {
		
	}

	@Override
	public WSEntity get(String name) {
		return map.get(name);
	}

}
