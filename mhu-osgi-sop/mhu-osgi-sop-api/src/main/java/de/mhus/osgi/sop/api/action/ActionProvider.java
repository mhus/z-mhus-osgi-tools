package de.mhus.osgi.sop.api.action;

import java.util.Collection;

import de.mhus.lib.basics.Named;
import de.mhus.lib.core.lang.Adaptable;

public interface ActionProvider extends Adaptable<Object>, Named {

	Collection<ActionDescriptor> getActions();

	ActionDescriptor getAction(String name);
	
}
