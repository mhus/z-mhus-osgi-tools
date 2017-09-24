package de.mhus.osgi.sop.impl.adb;

import java.util.List;

import de.mhus.lib.adb.DbAccessManager;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.DbObject;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.adb.transaction.DbLockObject;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.karaf.adb.DbManagerService;
import de.mhus.lib.sql.DbConnection;
import de.mhus.lib.sql.DbResult;
import de.mhus.osgi.sop.api.adb.AbstractDbSchema;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;

public class SopDbSchema extends AbstractDbSchema {

	private SopDbManagerService admin;
	private DbAccessManager accessManager;
	
	public SopDbSchema() {
		init();
	}
	
	public SopDbSchema(SopDbManagerService admin) {
		this.admin = admin;
		init();
	}
	
	private void init() {
		tablePrefix = MApi.getCfg(DbManagerService.class).getExtracted("tablePrefix", "sop_");
	}

	@Override
	public void findObjectTypes(List<Class<? extends Persistable>> list) {
		
		list.add(DbLockObject.class); // needed for object locking
		
		for (DbSchemaService schema : admin.getSchemas()) {
			schema.registerObjectTypes(list);
		}
		
	}


	@Override
	public Object createObject(Class<?> clazz, String registryName, DbResult ret, DbManager manager, boolean isPersistent) throws Exception {
		Object object = clazz.newInstance();
		if (object instanceof DbObject) {
			((DbObject)object).doInit(manager, registryName, isPersistent);
		}
		return object;
	}

	@Override
	public synchronized DbAccessManager getAccessManager(Table c) {
		if (accessManager == null)
			accessManager = new MyAccessManager();
		return accessManager;
	}
	
	private class MyAccessManager extends DbAccessManager {

		@Override
		public void hasAccess(DbManager manager, Table c, DbConnection con,
				Object object, ACCESS right) throws AccessDeniedException {

			if (object instanceof DbMetadata) {
				DbMetadata obj = (DbMetadata)object;
				try {
					AdbApi adb = MApi.lookup(AdbApi.class);
					switch(right) {
					case CREATE:
						if (!adb.canCreate(obj))
							throw new AccessDeniedException(c.getName(),right);
						break;
					case DELETE:
						if (!adb.canDelete(obj))
							throw new AccessDeniedException(c.getName(),right);
						break;
					case READ:
						if (!adb.canRead(obj))
							throw new AccessDeniedException(c.getName(),right);
						break;
					case UPDATE:
						if (!adb.canUpdate(obj))
							throw new AccessDeniedException(c.getName(),right);
						break;
					default:
						throw new AccessDeniedException(c.getName(),right,"unknown right");
					}
				} catch (AccessDeniedException ade) {
					throw ade;
				} catch (Throwable t) {
					log().d(t);
					throw new AccessDeniedException(c.getName(),right,t);
				}
			}
		}
	}

}
