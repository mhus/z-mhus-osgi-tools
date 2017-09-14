package de.mhus.osgi.sop.impl.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MLog;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.Node;
import de.mhus.osgi.sop.api.rest.RestApi;
import de.mhus.osgi.sop.api.rest.RestNodeService;

//@Component(immediate=true,name="RestService")
public class RestApiImpl extends MLog implements RestApi {

	private BundleContext context;
	private ServiceTracker<RestNodeService,RestNodeService> nodeTracker;
	private HashMap<String, RestNodeService> register = new HashMap<>();

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		nodeTracker = new ServiceTracker<>(context, RestNodeService.class, new RestNodeServiceTrackerCustomizer() );
		nodeTracker.open();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		nodeTracker.close();
		context = null;
		nodeTracker = null;
		register.clear();
	}

	private class RestNodeServiceTrackerCustomizer implements ServiceTrackerCustomizer<RestNodeService,RestNodeService> {

		@Override
		public RestNodeService addingService(
				ServiceReference<RestNodeService> reference) {

			RestNodeService service = context.getService(reference);
			if (service != null) {
				for (String x : service.getParentNodeIds()) {
					String key = x + "-" + service.getNodeId();
					log().i("register",key,service.getClass().getCanonicalName());
					register.put(key,service);
				}
			}
			
			return service;
		}

		@Override
		public void modifiedService(
				ServiceReference<RestNodeService> reference,
				RestNodeService service) {
			
		}

		@Override
		public void removedService(ServiceReference<RestNodeService> reference,
				RestNodeService service) {
			
			if (service != null) {
				for (String x : service.getParentNodeIds()) {
					String key = x + "-" + service.getNodeId();
					log().i("unregister",key,service.getClass().getCanonicalName());
					register.remove(key);
				}
			}

		}
		
	}
	
	@Override
	public Map<String, RestNodeService> getRestNodeRegistry() {
		return register;
	}

	@Override
	public Node lookup(List<String> parts, String lastNodeId, CallContext context) throws Exception {
		if (parts.size() < 1) return null;
		String name = parts.get(0);
		parts.remove(0);
		if (lastNodeId == null) lastNodeId = RestNodeService.ROOT_ID;
		RestNodeService next = register.get(lastNodeId + "-" + name); 
		if (next == null) return null;
		return next.lookup(parts, context);
	}
	
}
