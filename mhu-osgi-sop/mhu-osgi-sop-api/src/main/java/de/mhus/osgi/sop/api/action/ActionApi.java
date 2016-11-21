package de.mhus.osgi.sop.api.action;

import java.util.Collection;

import de.mhus.osgi.sop.api.SApi;

public interface ActionApi extends SApi {

	/**
	 * Returns a specified action by path or source/path
	 * If a action exists two times it's not defined which one will be returned.
	 * 
	 * @param name
	 * @return the action or null if not exists
	 */
	ActionDescriptor getAction(String name);
	
	/**
	 * Returns all actions.
	 * 
	 * @return never null
	 */
	Collection<ActionDescriptor> getActions();
	
	/**
	 * Returns actions matching the tags.
	 * 
	 * @param tags
	 * @return never null
	 */
	Collection<ActionDescriptor> getActions(Collection<String> tags);

	/**
	 * Returns the source or null if not exists.
	 * 
	 * @param name
	 * @return
	 */
	ActionProvider getSource(String name);
	
}
