package de.mhus.osgi.sop.api.model;

import java.util.HashSet;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.errors.MException;

public class BpmDefinition extends DbMetadata {

	@DbPersistent
	@DbIndex("u1")
	private String process;
	@DbPersistent
	@DbIndex("u1")
	private String processor;
	@DbPersistent
	private HashSet<String> parameters = new HashSet<>();
	@DbPersistent
	private boolean enabled = true;
	@DbPersistent
	private MProperties options = new MProperties();
	
	public BpmDefinition() {
	}
	
	public BpmDefinition(String processor) {
		this.processor = processor;
	}

	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public HashSet<String> getParameters() {
		return parameters;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public MProperties getOptions() {
		return options;
	}

	public String getPublicFilter(boolean created) {
		return created ? options.getString("filter.created", null) : options.getString("filter.public", null);
	}

	public String getProcessor() {
		return processor;
	}
	
}
