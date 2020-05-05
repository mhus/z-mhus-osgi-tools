package de.mhus.lib.mutable;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.core.MApi;
import de.mhus.osgi.api.services.MOsgi;

@Component(property = "event.topics=org/osgi/framework/ServiceEvent/MODIFIED")
public class KarafCfgUpdater implements EventHandler {

    @Override
    public void handleEvent(Event event) {
        try {
            @SuppressWarnings("unchecked")
            ServiceReference<Object> serviceRef = (ServiceReference<Object>) event.getProperty("service");
            Object service = MOsgi.getService(serviceRef);
            String pid = MOsgi.getPid(service.getClass());
            ((KarafCfgManager)MApi.get().getCfgManager()).update(pid);
            MApi.getCfgUpdater().doUpdate(pid);
        } catch (Throwable t) {
            t.printStackTrace(); // should not happen
        }
    }

}
