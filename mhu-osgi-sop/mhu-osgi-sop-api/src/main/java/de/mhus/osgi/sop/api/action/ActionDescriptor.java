package de.mhus.osgi.sop.api.action;

import java.util.Collection;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.util.ParameterDefinitions;

public class ActionDescriptor {
	private Action action;
	private Collection<String> tags;
	private String path;
	private String source;
	private ParameterDefinitions definitions;
	private String form;

	public ActionDescriptor(Action action, Collection<String> tags, String source, String path, ParameterDefinitions definitions, String form) {
		super();
		this.action = action;
		this.tags = tags;
		this.source = source;
		this.path = path;
		this.definitions = definitions;
		this.form = form;
	}
	
	public boolean canExecute(Collection<String> providedTags, IProperties properties) {
		// negative check
		for (String t : tags) {
			if (t.startsWith("*")) {
				if (!providedTags.contains(t.substring(1)))
					return false;
			} else
			if (t.startsWith("!")) {
				if (providedTags.contains(t.substring(1)))
					return false;
			}
		}
		// positive check
		for (String t : providedTags) {
			if (!tags.contains(t)) 
				return false;
		}
		return action.canExecute(properties);
	}

	public Action getAction() {
		return action;
	}

	public Collection<String> getTags() {
		return tags;
	}

	public String getName() {
		return path;
	}
	
	public String getSource() {
		return source;
	}

	/**
	 * Every action should have a parameter definition. If
	 * parameter definitions are not supported, the method will return null;
	 * @return
	 */
	public ParameterDefinitions getParameterDefinitions() {
		return definitions;
	}
	
	/**
	 * An action can provide a form component but it's not necessary. If
	 * parameter definitions are not supported, the method will return null;
	 * @return
	 */
	public String getForm() {
		return form;
	}
	
}
