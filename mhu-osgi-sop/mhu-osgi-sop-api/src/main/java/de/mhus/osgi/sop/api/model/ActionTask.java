package de.mhus.osgi.sop.api.model;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;

public class ActionTask extends DbMetadata {
	
	@DbPersistent(ro=true)
	@DbIndex({"xq","xqt"})
	private String queue;
	
	@DbPersistent
	private String action;
	@DbPersistent
	private String[] properties;
	@DbPersistent
	@DbIndex("xqt")
	private String target;

	public String getQueue() {
		return queue;
	}
	public void setQueue(String queue) {
		this.queue = queue;
	}
	
	public String toString() {
		return MSystem.toString(this, getId(), queue, getCreationDate(), action);
	}
		
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String[] getProperties() {
		return properties;
	}
	public void setProperties(String[] properties) {
		this.properties = properties;
	}
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}
	
}
