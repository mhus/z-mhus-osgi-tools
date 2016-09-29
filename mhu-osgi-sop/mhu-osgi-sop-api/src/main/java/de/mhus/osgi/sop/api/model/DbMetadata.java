package de.mhus.osgi.sop.api.model;

import java.util.Date;
import java.util.UUID;

import de.mhus.lib.adb.DbAccessManager;
import de.mhus.lib.adb.DbComfortableObject;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbPrimaryKey;
import de.mhus.lib.annotations.adb.DbTable;
import de.mhus.lib.basics.UuidIdentificable;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;
import de.mhus.lib.sql.DbConnection;

@DbTable(features=DbAccessManager.FEATURE_NAME)
public abstract class DbMetadata extends DbComfortableObject implements UuidIdentificable {

	@DbPrimaryKey
	private UUID id;
	
	@DbPersistent
	private Date creationDate;
	
	@DbPersistent
	private Date modifyDate;
	
	@DbPersistent
	private long vstamp;

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void doPreCreate(DbConnection con) {
		creationDate = new Date();
		modifyDate = creationDate;
		vstamp = 0;
	}
	
	@Override
	public void doPreSave(DbConnection con) {
		modifyDate = new Date();
		vstamp++;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public long getVstamp() {
		return vstamp;
	}

	public abstract DbMetadata findParentObject() throws MException;
	
	@Override
	public String toString() {
		return MSystem.toString(this,getId());
	}

}
