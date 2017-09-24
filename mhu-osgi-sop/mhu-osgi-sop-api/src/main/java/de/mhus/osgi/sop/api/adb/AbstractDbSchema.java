package de.mhus.osgi.sop.api.adb;

import java.util.HashMap;

import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.adb.transaction.MemoryLockStrategy;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.karaf.adb.DbManagerService;
import de.mhus.lib.sql.DbConnection;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.util.SopFileLogger;

public abstract class AbstractDbSchema extends DbSchema {

	private Log trace = new SopFileLogger(
			MApi.getCfg(DbManagerService.class).getExtracted("traceLoggerName","db"), getClass().getCanonicalName());

	public AbstractDbSchema() {
		trace.i("start");
		lockStrategy = new MemoryLockStrategy();
		((MemoryLockStrategy)lockStrategy).setMaxLockAge(
				MApi.getCfg(DbManagerService.class).getLong("maxLockAge", MTimeInterval.MINUTE_IN_MILLISECOUNDS * 5)
		);
	}
	
	@Override
	public void authorizeSaveForceAllowed(DbConnection con, Table table, Object object, boolean raw) throws AccessDeniedException {
		if (!MApi.lookup(AccessApi.class).getCurrentOrGuest().isAdminMode())
			throw new AccessDeniedException();
	}

	@Override
	public void authorizeUpdateAttributes(DbConnection con, Table table,
			Object object, boolean raw, String ... attributeNames) throws AccessDeniedException {
		if (!MApi.lookup(AccessApi.class).getCurrentOrGuest().isAdminMode())
			throw new AccessDeniedException();
	}

	@Override
	public void internalCreateObject(DbConnection con, String name, Object object,
			HashMap<String, Object> attributes) {
		super.internalCreateObject(con, name, object, attributes);
		trace.i("create",name,attributes,object);
	}

	@Override
	public void internalSaveObject(DbConnection con, String name, Object object,
			HashMap<String, Object> attributes) {
		super.internalSaveObject(con, name, object, attributes);
		trace.i("modify",name, attributes,object);
	}

	@Override
	public void internalDeleteObject(DbConnection con, String name, Object object,
			HashMap<String, Object> attributes) {
		super.internalDeleteObject(con, name, object, attributes);
		trace.i("delete",name, attributes,object);
	}

}
