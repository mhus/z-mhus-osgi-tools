package de.mhus.osgi.sop.api.adb;

import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;

public abstract class AbstractDbSchemaService implements DbSchemaService {

	@Override
	public boolean canRead(AaaContext account, DbMetadata obj)
			throws MException {
		return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(),obj.getClass().getName(), String.valueOf(obj.getId()), Account.ACT_READ);
	}

	@Override
	public boolean canUpdate(AaaContext account, DbMetadata obj)
			throws MException {
		return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(),obj.getClass().getName(), String.valueOf(obj.getId()), Account.ACT_UPDATE);
	}

	@Override
	public boolean canDelete(AaaContext account, DbMetadata obj)
			throws MException {
		return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(),obj.getClass().getName(), String.valueOf(obj.getId()), Account.ACT_DELETE);
	}

	@Override
	public boolean canCreate(AaaContext account, DbMetadata obj)
			throws MException {
		return MApi.lookup(AccessApi.class).hasResourceAccess(account.getAccount(),obj.getClass().getName(), String.valueOf(obj.getId()), Account.ACT_CREATE);
	}

	@Override
	public DbMetadata getObject(String type, UUID id) throws MException {
		try {
			Class<?> clazz = Class.forName(type, true, this.getClass().getClassLoader());
			if (clazz != null) {
				return (DbMetadata)MApi.lookup(AdbApi.class).getManager().getObject(clazz, id);
			}
		} catch (Throwable t) {
			throw new MException("type error",type,t);
		}
		throw new MException("unknown type",type);
	}

	@Override
	public DbMetadata getObject(String type, String id) throws MException {
		try {
			return getObject(type, UUID.fromString(id));
		} catch (Throwable t) {
			throw new MException("type error",type,t);
		}
	}
	
}
