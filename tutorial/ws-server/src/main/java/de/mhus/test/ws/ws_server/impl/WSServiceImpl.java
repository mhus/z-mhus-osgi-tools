package de.mhus.test.ws.ws_server.impl;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;


@SuppressWarnings("restriction")
@WebService(endpointInterface = "de.mhus.test.ws.ws_model.WSService")
@Component(name="HohohoHeHe",immediate=true)
public class WSServiceImpl implements WSService {

	private Logger log = Logger.getLogger(WSServiceImpl.class.getName());
	private HashMap<String, WSEntity> map = new HashMap<String, WSEntity>();
	private BundleContext context;
	private Endpoint handler;
	
	@Activate
	public void activate(ComponentContext ctx) {
		log.info("START");
		this.context = ctx.getBundleContext();

		handler = Endpoint.publish("/hehe", this);
		
	}

	@Deactivate
	public void deactivate(ComponentContext ctx) {
		log.info("STOP");
		handler.stop();
		
	}
	
	public WSServiceImpl() {
		addEntity(new WSEntity("alf")); // sample
	}
	
	public void addEntity(WSEntity entity) {
		if (entity == null) return;
		log.info("add " + entity.getName());
		map.put(entity.getName(), entity);
	}

	public WSEntity[] getAll() {
		return map.values().toArray(new WSEntity[0]);
	}

	public void removeEntity(WSEntity entity) {
		if (entity == null) return;
		log.info("remove " + entity.getName());
		map.remove(entity.getName());
	}

	@Override
	public WSEntity get(String name) {
		return map.get(name);
	}

}
