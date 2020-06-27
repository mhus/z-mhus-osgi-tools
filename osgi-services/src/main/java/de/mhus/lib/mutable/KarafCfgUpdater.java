package de.mhus.lib.mutable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.core.MApi;
import de.mhus.osgi.api.MOsgi;

@Component(property = "event.topics=org/osgi/framework/ServiceEvent/MODIFIED")
public class KarafCfgUpdater implements EventHandler {

	
	private BundleContext ctx;

	@Activate
	public void doActivate(BundleContext ctx) {
		this.ctx = ctx;
	}

    @Override
    public void handleEvent(Event event) {
        try {
            @SuppressWarnings("unchecked")
            ServiceReference<Object> serviceRef = (ServiceReference<Object>) event.getProperty("service");
            Object service = ctx.getService(serviceRef);
            String pid = MOsgi.getPid(service.getClass());
            ((KarafCfgManager)MApi.get().getCfgManager()).update(pid);
            MApi.getCfgUpdater().doUpdate(pid);
        } catch (Throwable t) {
            t.printStackTrace(); // should not happen
        }
    }

}
