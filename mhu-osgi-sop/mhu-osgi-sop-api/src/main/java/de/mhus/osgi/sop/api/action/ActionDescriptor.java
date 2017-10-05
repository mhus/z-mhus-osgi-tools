package de.mhus.osgi.sop.api.action;

import java.util.Collection;

import de.mhus.lib.basics.Named;
import de.mhus.lib.basics.Versioned;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.util.MNls;
import de.mhus.lib.core.util.MNlsProvider;
import de.mhus.lib.core.util.Nls;
import de.mhus.lib.core.util.ParameterDefinitions;

public class ActionDescriptor implements MNlsProvider, Nls, Named, Versioned {
	
	private Action action;
	private Collection<String> tags;
	private String path;
	private String version;
	private String source;
	private ParameterDefinitions definitions;
	private DefRoot form;
	private MNls nls;
	private MNlsProvider nlsProvider;
	private String title;

	public ActionDescriptor(
			Action action, 
			Collection<String> tags, 
			String source, 
			String path,
			String version,
			ParameterDefinitions definitions, 
			DefRoot form, 
			MNlsProvider nlsProvider, 
			String title
		) {
		super();
		this.action = action;
		this.tags = tags;
		this.source = source;
		this.path = path;
		this.version = version;
		this.definitions = definitions;
		this.form = form;
		this.nlsProvider = nlsProvider;
		this.title = title;
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
			if (t.startsWith("!")) {
				if (tags.contains(t.substring(1)))
					return false;
			} else
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

	@Override
	public String getName() {
		return path;
	}
	
	@Override
	public String getVersion() {
		return version;
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
	public DefRoot getForm() {
		return form;
	}

	@Override
	public String nls(String text) {
		return MNls.find(this, text);
	}

	@Override
	public MNls getNls() {
		if (nls == null)
			nls = nlsProvider.getNls();
		return nls;
	}

	public String getCaption() {
		return nls("caption=" + getTitle());
	}

	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, path, tags);
	}
	
}
