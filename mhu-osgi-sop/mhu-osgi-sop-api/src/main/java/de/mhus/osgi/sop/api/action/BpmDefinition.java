package de.mhus.osgi.sop.api.action;

import java.util.HashMap;
import java.util.HashSet;

import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MString;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.model.DbMetadata;

public class BpmDefinition extends DbMetadata {

	@DbPersistent
	@DbIndex("u1")
	private String process;
	@DbPersistent
	private HashMap<String, String> mapping = new HashMap<String, String>();
	@DbPersistent
	private HashSet<String> parameters = new HashSet<String>();
	@DbPersistent
	private boolean enabled = true;
	@DbPersistent
	private HashMap<String, String> options = new HashMap<String, String>();
	@DbPersistent
	private String operation;
	
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

	public HashMap<String, String> getMapping() {
		return mapping;
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

	public HashMap<String, String> getOptions() {
		return options;
	}

	public String getPublicFilter(boolean created) {
		return created ? options.get("filter.created") : options.get("filter.public");
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public boolean isOperation() {
		return MString.isSet(getOperation());
	}
	
	public boolean isProcess() {
		return MString.isEmpty(getOperation());
	}

}
