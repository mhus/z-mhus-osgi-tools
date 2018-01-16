package de.mhus.karaf.mongo;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.MServiceTracker;

@Component(immediate=true)
public class MoManagerAdminImpl extends MLog implements MoManagerAdmin {

	private MServiceTracker<MoManagerService> tracker;
	private LinkedList<MoManagerService> services = new LinkedList<>();
	private HashMap<String,MoManagerService> servicesByName = new HashMap<>();

	@Activate
	public void doActivate(ComponentContext ctx) {
		tracker = new MServiceTracker<MoManagerService>(MoManagerService.class) {
			
			@Override
			protected void removeService(ServiceReference<MoManagerService> reference, MoManagerService service) {
				MoManagerAdminImpl.this.removeService(service);
			}
			
			@Override
			protected void addService(ServiceReference<MoManagerService> reference, MoManagerService service) {
				try {
					MoManagerAdminImpl.this.addService(service);
				} catch (Exception e) {
					log().e(reference,e);
				}
			}
		};
		tracker.start();
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		if (tracker != null) {
			tracker.stop();
			tracker = null;
		}
	}
	

	@Override
	public void addService(MoManagerService service) throws Exception {
		if (service == null) return;
		service.doInitialize();
		synchronized (services) {
			services.add(service);
			String n = service.getServiceName();
			if (n != null)
				servicesByName.put(n, service);
			else
				log().d("MoService has no name",service);
		}
	}

	@Override
	public void removeService(MoManagerService service) {
		if (service == null) return;
		synchronized (services) {
			services.remove(service);
			String n = service.getServiceName();
			if (n != null)
				servicesByName.remove(service.getServiceName());
			if (servicesByName.containsValue(service))
				servicesByName.entrySet().removeIf(e -> e.getValue() == service);
		}
		service.doClose();
	}

	@Override
	public MoManagerService getService(String name) throws NotFoundException {
		if (name == null) throw new NullPointerException();
		synchronized (services) {
			MoManagerService s = servicesByName.get(name);
			if (s == null) 
				throw new NotFoundException("MoService not found",name);
			return s;
		}
	}
	
	public Collection<MoManagerService> getServices() {
		synchronized (services) {
			return new LinkedList<>(services);
		}		
	}
	

}
