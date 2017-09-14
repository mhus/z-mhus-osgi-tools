package de.mhus.osgi.sop.impl.action;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.karaf.MServiceMap;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.action.ActionApi;
import de.mhus.osgi.sop.api.action.ActionDescriptor;
import de.mhus.osgi.sop.api.action.ActionProvider;

@Component
public class ActionApiImpl extends MLog implements ActionApi {

    public static ActionApiImpl instance;
    private MServiceMap<ActionProvider> serviceProviders = null;

	@Activate
    public void activate(ComponentContext ctx) {
    	serviceProviders = new MServiceMap<ActionProvider>(ctx.getBundleContext(), ActionProvider.class) {
    		@Override
			protected String getServiceName(ServiceReference<ActionProvider> reference, ActionProvider service) {
    			return service.getName();
    		}
    	};
    	serviceProviders.start();
    	instance = this;
    }
    
    @Deactivate
    public void deactivate(ComponentContext ctx) {
    	instance = null;
    	serviceProviders.stop();
    	serviceProviders = null;
    }
	
	@Override
	public ActionDescriptor getAction(String name) {
		if (name == null) return null;
		if (MString.isIndex(name, ':')) {
			String providerName = MString.beforeIndex(name, ':');
			name = MString.afterIndex(name, ':');
			ActionProvider p = getProvider(providerName);
			if (p == null) return null;
			ActionDescriptor a = p.getAction(name);
			if (a == null) return null;
			if (!hasAccess(a)) return null;
			return a;
		}
		
		for (ActionProvider p : getProviders()) {
			ActionDescriptor a = p.getAction(name);
			if (a != null) return a;
		}
		return null;
	}

	private boolean hasAccess(ActionDescriptor a) {
		AccessApi api = MApi.lookup(AccessApi.class);
		if (api == null) return true; // no access api support
		AaaContext context = api.getCurrentOrGuest();
		if (context.isAdminMode()) return true;
		boolean access = api.hasGroupAccess(context.getAccount(), ActionApi.class, a.getSource() + "_" + a.getName(), "execute");
		return access;
	}

	private ActionProvider[] getProviders() {
		return serviceProviders.getServices();
	}

	private ActionProvider getProvider(String providerName) {
		return serviceProviders.getService(providerName);
	}

	@Override
	public Collection<ActionDescriptor> getActions() {
		LinkedList<ActionDescriptor> out = new LinkedList<>(); 
		for (ActionProvider p : getProviders()) {
			try {
				for (ActionDescriptor a : p.getActions())
					try {
						if (hasAccess(a))
							out.add(a);
					} catch (Throwable t) {
						log().d("can't load action", p, a, t);
					}
			} catch (Throwable t) {
				log().d("can't load from action provider", p, t);
			}
		}
		return out;
	}

	@Override
	public Collection<String> getActionPathes() {
		LinkedList<String> out = new LinkedList<>(); 
		for (ActionProvider p : getProviders()) {
			String providerName = p.getName();
			for (ActionDescriptor a : p.getActions())
				if (hasAccess(a))
					out.add(providerName + ':' + a.getName());
		}
		return out;
	}
	
	@Override
	public List<ActionDescriptor> getActions(Collection<String> tags, IProperties properties) {
		LinkedList<ActionDescriptor> out = new LinkedList<>(); 
		for (ActionProvider p : getProviders()) {
			for (ActionDescriptor a : p.getActions())
				if (hasAccess(a))
					if (a.canExecute(tags, properties))
						out.add(a);
		}
		return out;
	}

}
