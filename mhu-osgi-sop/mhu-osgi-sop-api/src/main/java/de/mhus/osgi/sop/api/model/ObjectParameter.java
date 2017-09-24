package de.mhus.osgi.sop.api.model;

import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;

public class ObjectParameter extends DbMetadata {

	public static final String TYPE_GLOBAL = "_global";

	@DbIndex({"1","2","3"})
	@DbPersistent
	private String objectType;
	@DbIndex({"1","2"})
	@DbPersistent
	private UUID   objectId;
	@DbIndex({"1","3"})
	@DbPersistent
	private String key;
	@DbPersistent
	@DbIndex("3")
	private String value;
	
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public UUID getObjectId() {
		return objectId;
	}
	public void setObjectId(UUID objectId) {
		this.objectId = objectId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return MSystem.toString(this,getId(),objectType,objectId,key,value);
	}
	@Override
	public DbMetadata findParentObject() throws MException {
		return null;
	}

}
