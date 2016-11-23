package de.mhus.osgi.sop.api.action;

import java.util.Collection;

import de.mhus.lib.core.IProperties;

public class ActionDescriptor {
	private Action action;
	private Collection<String> tags;
	private String path;
	private String source;

	public ActionDescriptor(Action action, Collection<String> tags, String source, String path) {
		super();
		this.action = action;
		this.tags = tags;
		this.source = source;
		this.path = path;
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
	
}
