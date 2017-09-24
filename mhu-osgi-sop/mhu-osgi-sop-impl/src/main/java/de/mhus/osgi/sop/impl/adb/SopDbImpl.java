package de.mhus.osgi.sop.impl.adb;

import java.util.List;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.lib.karaf.adb.DbManagerService;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.Reference;
import de.mhus.osgi.sop.api.adb.Reference.TYPE;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.model.ActionTask;
import de.mhus.osgi.sop.api.model.ObjectParameter;
import de.mhus.osgi.sop.api.model.Register;

@Component(provide=DbSchemaService.class,immediate=true)
public class SopDbImpl extends MLog implements DbSchemaService {

	private DbManagerService service;
	private static SopDbImpl instance;

	public static SopDbImpl instance() {
		return instance;
	}
	
	@Override
	public void registerObjectTypes(List<Class<? extends Persistable>> list) {
		list.add(ObjectParameter.class);
		list.add(ActionTask.class);
		list.add(Register.class);
	}

	@Override
	public void doInitialize(DbManagerService ngnDbService) {
		this.service = ngnDbService;
		instance = this;
	}

	@Override
	public void doDestroy() {
		instance = null;
		service = null;
	}

	public static DbManager getManager() {
		return instance
				.service
				.getManager();
	}
	
	@Override
	public boolean canRead(AaaContext account, DbMetadata obj)
			throws MException {
		
		if (obj instanceof ObjectParameter) {
			ObjectParameter o = (ObjectParameter)obj;
			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
			
			String type = o.getObjectType();
			if (type == null) return false;
			if (type.equals(ObjectParameter.class.getCanonicalName())) return true;
			
			
//			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
//			if (ace == null) return false;
//			return ace.canRead();
			
			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_READ);
		}
		
		return false;
	}

	@Override
	public boolean canUpdate(AaaContext account, DbMetadata obj)
			throws MException {
		if (obj instanceof ObjectParameter) {
			ObjectParameter o = (ObjectParameter)obj;
			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
			
			String type = o.getObjectType();
			if (type == null) return false;
			if (type.equals(ObjectParameter.class.getCanonicalName())) return true;
			
//			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
//			if (ace == null) return false;
//			return ace.canUpdate();
			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_UPDATE);

		}
		return false;
	}

	@Override
	public boolean canDelete(AaaContext account, DbMetadata obj)
			throws MException {
		if (obj instanceof ObjectParameter) {
			ObjectParameter o = (ObjectParameter)obj;
			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
			
			String type = o.getObjectType();
			if (type == null) return false;
			if (type.equals(ObjectParameter.class.getCanonicalName())) return true;
			
//			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
//			if (ace == null) return false;
//			return ace.canDelete();
			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_DELETE);
		}
		return false;
	}

	@Override
	public boolean canCreate(AaaContext account, DbMetadata obj)
			throws MException {
		
		if (obj instanceof ActionTask)
			return true;
		
		if (obj instanceof ObjectParameter) {
			ObjectParameter o = (ObjectParameter)obj;
			if (o.getKey() == null || o.getKey().startsWith("private.")) return false;
			
			String type = o.getObjectType();
			if (type == null) return false;
			if (type.equals(ObjectParameter.class.getCanonicalName())) return true;
			
//			Ace ace = Sop.getApi(SopApi.class).findAce(account.getAccountId(), type, o.getObjectId() );
//			if (ace == null) return false;
//			return ace.canCreate();
			return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(), type, String.valueOf(o.getObjectId()), Account.ACT_CREATE);
		}
		
		return false;
	}

	@Override
	public DbMetadata getObject(String type, UUID id) throws MException {
		if (type.equals(ObjectParameter.class.getCanonicalName()))
			return SopDbImpl.getManager().getObject(ObjectParameter.class, id);
		if (type.equals(ActionTask.class.getCanonicalName()))
			return SopDbImpl.getManager().getObject(ActionTask.class, id);
		throw new MException("unknown type",type);
	}

	@Override
	public DbMetadata getObject(String type, String id) throws MException {
		return getObject(type, UUID.fromString(id));
	}

	@Override
	public void collectReferences(DbMetadata object,
			ReferenceCollector collector) {
		if (object == null) return;
		try {
			for (ObjectParameter p : MApi.lookup(AdbApi.class).getParameters(object.getClass(), object.getId())) {
				collector.foundReference(new Reference<DbMetadata>(p,TYPE.CHILD));
			}
		} catch (MException e) {
			log().d(object.getClass(),object.getId(),e);
		}
	}

	@Override
	public void doCleanup() {
		// TODO Auto-generated method stub
		
	}
	
}
