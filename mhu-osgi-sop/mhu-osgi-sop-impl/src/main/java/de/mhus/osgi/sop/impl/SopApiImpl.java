package de.mhus.osgi.sop.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.codehaus.jackson.node.ObjectNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.cfg.CfgProperties;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.Account;
import de.mhus.osgi.sop.api.aaa.AccountSource;
import de.mhus.osgi.sop.api.aaa.AuthorizationSource;
import de.mhus.osgi.sop.api.aaa.ContextCachedItem;
import de.mhus.osgi.sop.api.aaa.Reference;
import de.mhus.osgi.sop.api.aaa.Reference.TYPE;
import de.mhus.osgi.sop.api.aaa.ReferenceCollector;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.aaa.TrustSource;
import de.mhus.osgi.sop.api.action.BpmApi;
import de.mhus.osgi.sop.api.action.BpmCase;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.model.ActionTask;
import de.mhus.osgi.sop.api.model.DbMetadata;
import de.mhus.osgi.sop.api.model.ObjectParameter;
import de.mhus.osgi.sop.api.rest.AbstractNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.RestResult;
import de.mhus.osgi.sop.api.rest.RestUtil;
import de.mhus.osgi.sop.api.util.TicketUtil;
import de.mhus.osgi.sop.impl.adb.SopDbImpl;

@Component(immediate=true,provide=SopApi.class,name="SopApi")
public class SopApiImpl extends MLog implements SopApi {
	
	private static AaaContextImpl ROOT_CONTEXT = new RootContext();
	private WeakHashMap<String, Account> accountCache = new WeakHashMap<String, Account>();
	private WeakHashMap<String, Trust> trustCache = new WeakHashMap<String, Trust>();

	private BundleContext context;
	private ServiceTracker<DbSchemaService,DbSchemaService> accessTracker;
	private HashMap<String, DbSchemaService> controllers = new HashMap<String, DbSchemaService>();
	
	private AccountSource accountSource;
	private TrustSource trustSource;
	private AuthorizationSource authorizationSource;
	
	private CfgProperties config = new CfgProperties(SopApi.class, "sop");
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		accessTracker = new ServiceTracker<>(context, DbSchemaService.class, new MyAccessTrackerCustomizer() );
		accessTracker.open();
		
		// TODO set synchronizer
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		accessTracker.close();
		accessTracker = null;
		context = null;
		controllers.clear();

	}
	
	@Override
	public ActionTask createActionTask(String queue, String action, String target, String[] properties, boolean smart) throws MException {

		DbManager manager = getDbManager();	

		if (smart) {
			ActionTask t = manager.getObjectByQualification(Db.query(ActionTask.class).eq("queue", queue).eq("action", action).eq("target", target));
			if (t != null) return t;
		}		
		
		ActionTask task = manager.inject(new ActionTask());
		task.setQueue(queue);
		task.setAction(action);
		task.setProperties(properties);
		task.setTarget(target);
		
		task.save();
		
		return task;
	}

	@Override
	public List<ActionTask> getQueue(String queue, int max) throws MException {
		LinkedList<ActionTask> out = new LinkedList<ActionTask>();
		DbManager manager = getDbManager();	
		
		DbCollection<ActionTask> res = manager.getByQualification(Db.query(ActionTask.class).eq(Db.attr("queue"), Db.value(queue)));
		for (ActionTask task : res) {
			out.add(task);
			if (max > 0 && out.size() >= max) break;
		}
		res.close();
		return out;
	}

	public List<ObjectParameter> getParameters(Class<?> type, UUID id) throws MException {
		return getParameters(type.getCanonicalName(), id);
	}
	
	public List<ObjectParameter> getParameters(String type, UUID id) throws MException {
		
		
		DbManager manager = getDbManager();
		List<ObjectParameter> out = manager.getByQualification(
				Db.query(ObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type))
				.eq(Db.attr("objectid"),Db.value(id))
				).toCacheAndClose();
		
		return out;
	}

	public void setGlobalParameter(String key, String value) throws MException {
		setParameter(ObjectParameter.TYPE_GLOBAL, MConstants.EMPTY_UUID, key, value);
	}

	public void setParameter(Class<?> type, UUID id, String key, String value) throws MException {
		setParameter(type.getCanonicalName(), id, key, value);
	}
	
	public void setParameter(String type, UUID id, String key, String value) throws MException {
				
		ObjectParameter out = getParameter(type, id, key);
		if (out == null) {
			DbManager manager = getDbManager();
			if (key == null) return;
			out = manager.inject(new ObjectParameter());
			out.setObjectType(type);
			out.setObjectId(id);
			out.setKey(key);
		} else if (value == null) {
			out.delete();
			return;
		}
		out.setValue(value);
		out.save();
	}
	
//	public ObjectParameter getRecursiveParameter(String type, UUID id, String key) throws MException {
//		
//		ObjectParameter out = null;
//		while (out == null && type != null) {
//			out = getParameter(type, id, key);
//			if (out == null) {
//				id = getParentId(type,id);
//				type = getTypeParent(type);
//			}
//		}
//		
//		return out;
//	}
	
//	public String getRecursiveValue(String type, UUID id, String key, String def) {
//		try {
//			ObjectParameter out = getRecursiveParameter(type, id, key);
//			return out == null || out.getValue() == null ? def : out.getValue();
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//		return def;
//	}
	
	public ObjectParameter getGlobalParameter(String key) throws MException {
		return getParameter(ObjectParameter.TYPE_GLOBAL, MConstants.EMPTY_UUID, key);
	}
	
	public String getValue(Class<?> type, UUID id, String key, String def) throws MException {
		return getValue(type.getCanonicalName(), id, key, def);
	}
	
	public String getValue(String type, UUID id, String key, String def) throws MException {
		ObjectParameter p = getParameter(type, id, key);
		if (p == null || p.getValue() == null) return def;
		return p.getValue();
	}
	
	public ObjectParameter getParameter(Class<?> type, UUID id, String key) throws MException {
		return getParameter(type.getCanonicalName(), id, key);
	}
	
	public ObjectParameter getParameter(String type, UUID id, String key) throws MException {
		
		DbManager manager = getDbManager();
		ObjectParameter out = manager.getByQualification(
				Db.query(ObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type))
				.eq(Db.attr("objectid"),Db.value(id))
				.eq(Db.attr("key"), Db.value(key))
				).getNextAndClose();
		return out;
	}

	@Override
	public ObjectParameter getRecursiveParameter(DbMetadata obj, String key) throws MException {
		int level = 10;
		while (obj != null && level > 0) {
			if (obj == null || key == null) return null;
			ObjectParameter out = getParameter(obj.getClass().getCanonicalName(), obj.getId(), key);
			if (out != null) return out;
			obj = obj.findParentObject();
			level--;
		}
		return null;
	}

	public List<UUID> getIds(String type, String key, String value) throws MException {

		LinkedList<UUID> out = new LinkedList<>();
		DbManager manager = getDbManager();
		for ( ObjectParameter p : manager.getByQualification(
				Db.query(ObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type))
				.eq(Db.attr("key"),Db.value(key))
				.eq(Db.attr("value"), Db.value(value))
				)) {
				out.add(p.getObjectId());
		}
		return out;
	}

	public void deleteAll(String type, UUID id) throws MException {

		for (ObjectParameter p : getParameters( type, id))
			if (canDelete(p))
				p.delete();
	}

	
	public DbManager getDbManager() {
		return SopDbImpl.getManager();
	}

	@Override
	public void deleteParameters(Class<?> type, UUID id) throws MException {
		for (ObjectParameter p : getParameters( type, id))
			p.delete();
	}

	@Override
	public List<ObjectParameter> getParameters(Class<?> type, String key,
			String value) throws MException {
		LinkedList<ObjectParameter> out = new LinkedList<>();
		DbManager manager = SopDbImpl.getManager();
		for ( ObjectParameter p : manager.getByQualification(
				Db.query(ObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type.getCanonicalName()))
				.eq(Db.attr("key"),Db.value(key))
				.eq(Db.attr("value"), Db.value(value))
				)) {
			out.add(p);
		}
		return out;
	}

	@Override
	public <T> LinkedList<T> collectResults(AQuery<T> query, int page) throws MException {
		LinkedList<T> list = new LinkedList<T>();
		DbCollection<T> res = getDbManager().getByQualification(query);
		if (!res.skip(page * SopApi.PAGE_SIZE)) return list;
		while (res.hasNext()) {
			list.add(res.next());
			if (list.size() >= PAGE_SIZE) break;
		}
		res.close();
		return list;
	}

	@Override
	public IProperties getMainConfiguration() {
		return config.value();
	}

	@SuppressWarnings("unchecked")
	@Override
	public RestResult doExecuteRestAction(CallContext callContext, String action, String source) throws MException {

		BpmApi bpm = Sop.getApi(BpmApi.class);
		
		long timeout = MTimeInterval.MINUTE_IN_MILLISECOUNDS * 10;
		
		HashMap<String, Object> parameters = new HashMap<>();
		for (String name : callContext.getParameterNames()) {
			if (!name.startsWith(AbstractNode.INTERNAL_PREFIX))
				parameters.put(name, callContext.getParameter(name));
		}

		for (String name : callContext.getNames()) {
//			if (name.endsWith(AbstractNode.ID)) {
//				parameters.put(name, String.valueOf(callContext.get(name)));
//			} else 
			if (name.endsWith(AbstractNode.OBJECT)) {
				Object val = callContext.get(name);
				if (val instanceof DbMetadata)
					parameters.put( RestUtil.getObjectIdParameterName( (Class<? extends DbMetadata>) val.getClass() ), String.valueOf( ((DbMetadata)val).getId() ) );
			}
		}
		
		parameters.put(AbstractNode.SOURCE, source );
		
		if (action == null) action = callContext.getAction();
		BpmCase res = bpm.createCase(null, action, parameters, true, timeout);
		
		DbManager manager = Sop.getApi(SopApi.class).getDbManager();
		DbSchema schema = manager.getSchema();
		JsonResult result = new JsonResult();
		ObjectNode jRoot = result.createObjectNode();
		try {
			MPojo.pojoToJson(res, jRoot, schema);
		} catch (IOException e) {
			log().e(e);
		}

		return result;
	}

	@Override
	public List<ActionTask> getActionTaskPage(String queue, int size) {
		
		LinkedList<ActionTask> out = new LinkedList<ActionTask>();
		try {
			DbCollection<ActionTask> res = getDbManager().getByQualification(Db.query(ActionTask.class).eq("queue", queue).desc("creationdate"));
			while (res.hasNext()) {
				out.add(res.next());
				if (out.size() >= size) break;
			}
			res.close();
		} catch (Throwable t) {
			log().e(queue,t);
		}
		return out;
	}


	protected DbSchemaService getController(String type) throws MException {
		if (type == null) throw new MException("type is null");
		DbSchemaService ret = controllers.get(type);
		if (ret == null) throw new MException("Access Controler not found",type);
		return ret;
	}

	private class MyAccessTrackerCustomizer implements ServiceTrackerCustomizer<DbSchemaService,DbSchemaService>{

		@Override
		public DbSchemaService addingService(
				ServiceReference<DbSchemaService> reference) {

			DbSchemaService service = context.getService(reference);
			if (service != null) {
				LinkedList<Class<? extends Persistable>> list = new LinkedList<>();
				service.registerObjectTypes(list);
				synchronized (controllers) {
					for (Class<?> clazz : list) {
						log().i("register access controller",clazz,service.getClass().getCanonicalName());
						DbSchemaService last = controllers.put(clazz.getCanonicalName(),service);
						if (last != null)
							log().w("overwrote access controller",clazz,service.getClass().getCanonicalName());
					}
				}
			}
			return service;
		}

		@Override
		public void modifiedService(ServiceReference<DbSchemaService> reference,
				DbSchemaService service) {

		}

		@Override
		public void removedService(ServiceReference<DbSchemaService> reference,
				DbSchemaService service) {
			
			if (service != null) {
				LinkedList<Class<? extends Persistable>> list = new LinkedList<>();
				service.registerObjectTypes(list);
				synchronized (controllers) {
					for (Class<?> clazz : list) {
						log().i("remove access controller",clazz,service.getClass().getCanonicalName());
						controllers.remove(clazz.getCanonicalName());
					}
				}
			}
			
		}
		
	}


	@Override
	public AaaContext process(String ticket) {
		
		if (ticket == null)
			throw new AccessDeniedException("null");
		boolean admin = false;
		String account = null;
		Account info = null;
		Trust trustInfo = null;
		
		String[] parts = ticket.split(TicketUtil.SEP);
		if (parts.length > 0 && parts[0].equals(TicketUtil.ACCOUNT)) {
			
			// ACCOUNT AUTH
			
			String pass = null;
			if (parts.length > 2) {
				account = parts[1];
				pass = parts[2];
			}
			if (parts.length > 3)
				admin = parts[3].equals(TicketUtil.ADMIN);
			
			if (account == null || pass == null)
				throw new AccessDeniedException("account or password not set");

			log().d("account",account);
			
			try {
				info = getAccount(account);
			} catch (MException e) {
				log().w(account,e);
			}
			if (info == null)
				throw new AccessDeniedException("null",account);
			if (!info.validatePassword(MPassword.decode(pass)))
				throw new AccessDeniedException("password",account);
			if (!info.isValide())
				throw new AccessDeniedException("invalid",account);
			
		} else
		if (parts.length > 0 && parts[0].equals(TicketUtil.TRUST)) {
			
			// TRUST AUTH
			
			String trust = null;
			String secret = null;
			if (parts.length > 3) {
				trust = parts[1];
				secret = parts[2];
				account = parts[3];
			}
			if (parts.length > 4)
				admin = parts[4].equals(TicketUtil.ADMIN);

			trustInfo = getTrust(trust);
			if (trustInfo == null)
				throw new AccessDeniedException("null",account);
			if (!trustInfo.validatePassword(MPassword.decode(secret)))
				throw new AccessDeniedException("password",account);
			if (!trustInfo.isValide())
				throw new AccessDeniedException("invalid",account);
			try {
				info = getAccountUnsecure(account);
			} catch (MException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (info == null)
				throw new AccessDeniedException("null",account);
			if (!info.isValide())
				throw new AccessDeniedException("invalid",account);
			
		} else
			throw new AccessDeniedException("unknown ticket type",parts[0]);
				
		return process(info, trustInfo, admin);
	}
	
	public AaaContextImpl process(Account info, Trust trust, boolean admin) {
		AaaContextImpl c = null;
		try {
			c = new AaaContextImpl(info,trust,admin);
		} catch (MException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (c == null) 
			throw new AccessDeniedException("null");
		ContextPool.getInstance().set(c);
		return c;
	}

	public synchronized Trust getTrust(String trust) {
		
		if (trust == null)
			throw new AccessDeniedException("null");

		AaaContext c = getCurrent();
		if (!c.isAdminMode())
			throw new AccessDeniedException("admin only");

		Trust out = trustCache.get(trust);
		if (out != null) {
			if ( out.isChanged() ) out = null;
		}

		if (out == null && trustSource != null) {
			out = trustSource.findTrust(trust);
			if (out != null) {
				trustCache.put(trust, out);
			} else {
				log().i("trust not found",trust);
			}
		}
		if (out != null)
			return out;

		throw new AccessDeniedException("trust not found",trust);

	}
	
	public synchronized Account getAccount(String account) throws MException {
		
		if (account == null)
			throw new AccessDeniedException("null");

		AaaContext c = getCurrent();
		if (!c.isAdminMode() && !c.getAccountId().equals(account))
			throw new AccessDeniedException("admin only");
		
		return getAccountUnsecure(account);
		
	}
	
	protected synchronized Account getAccountUnsecure(String account) throws MException {
		
		if (account.equals("root"))
			return ROOT_CONTEXT.getAccount();
		
		Account out = accountCache.get(account);
		if (out != null) {
			if (out instanceof AccountFile)
				if ( ((AccountFile)out).isChanged() ) {
					out = null;
					accountCache.remove(account);
				}
		}
		if (out == null && accountSource != null) {
			out = accountSource.findAccount(account);
		}
		if (out != null)
			return out;
		
		throw new AccessDeniedException("account not found",account);
	}

	@Override
	public AaaContext release(String ticket) {
		AaaContextImpl current = (AaaContextImpl) getCurrent();
		if (MString.isEmpty(ticket)) return current;
		String account = null;
		
		String[] parts = ticket.split(TicketUtil.SEP);
		if (parts.length > 0 && parts[0].equals(TicketUtil.ACCOUNT)) {
			if (parts.length > 2) {
				account = parts[1];
			}
		} else
		if (parts.length > 0 && parts[0].equals(TicketUtil.TRUST)) {
			if (parts.length > 3) {
				account = parts[3];
			}
		} else
			throw new AccessDeniedException("unknown ticket type",parts[0]);

		log().d("release",account);
		
		Account info = null;
		try {
			info = getAccount(account);
		} catch (MException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return release(info);
	}
	
	public AaaContext release(Account info) {
		AaaContextImpl current = (AaaContextImpl) getCurrent();
		String account = info.getAccount();
		ContextPool pool = ContextPool.getInstance();
		synchronized (pool) {
			current = pool.getCurrent();
			try {
				if (MString.isEmpty(account) || current == null || !account.equals(current.getAccount().getAccount()) ) return current;
			} catch (MException e) {
				e.printStackTrace();
				return current;
			}
			AaaContextImpl parent = current.getParent();
			pool.set(parent);
			return parent;
		}
	}

	public AaaContext release(AaaContext context) {
		ContextPool pool = ContextPool.getInstance();
		synchronized (pool) {
			if (context != null) {
				AaaContextImpl parent = ((AaaContextImpl)context).getParent();
				pool.set(parent);
				return parent;
			}
		}
		return null;
	}
	
	public void resetContext() {
		ContextPool pool = ContextPool.getInstance();
		synchronized (pool) {
			pool.set(null);
		}
	}
	
	@Override
	public AaaContext getCurrent() {
		AaaContextImpl out = ContextPool.getInstance().getCurrent();
		if (out == null) out = ROOT_CONTEXT;
		return out;
	}

	@Override
	public Account getCurrenAccount() throws MException {
		return getCurrent().getAccount();
	}

	protected boolean canRead(AaaContext c, DbMetadata obj)
			throws MException {
		
		ContextCachedItem item = ((AaaContextImpl)c).getCached("ace_read"+ "|" + obj.getId());
		if (item != null)
			return item.bool;

		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;

		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canRead(c,obj);
		((AaaContextImpl)c).setCached("ace_read"+ "|" + obj.getId(),ret);
		return ret.bool;
	}

	protected boolean canUpdate(AaaContext c, DbMetadata obj)
			throws MException {
		
		ContextCachedItem item = ((AaaContextImpl)c).getCached("ace_update"+ "|" + obj.getId());
		if (item != null)
			return item.bool;

		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;
		
		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canUpdate(c,obj);
		((AaaContextImpl)c).setCached("ace_update"+ "|" + obj.getId(),ret);
		return ret.bool;
	}

	protected boolean canDelete(AaaContext c, DbMetadata obj)
			throws MException {
		
		ContextCachedItem item = ((AaaContextImpl)c).getCached("ace_delete"+ "|" + obj.getId());
		if (item != null)
			return item.bool;

		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;

		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canDelete(c,obj);
		((AaaContextImpl)c).setCached("ace_delete"+ "|" + obj.getId(),ret);
		return ret.bool;
	}

	protected boolean canCreate(AaaContext c, DbMetadata obj) throws MException {
		ContextCachedItem item = ((AaaContextImpl)c).getCached("ace_create"+ "|" + obj.getId());
		if (item != null)
			return item.bool;
		
		DbSchemaService controller = getController(obj.getClass().getCanonicalName());
		if (controller == null) return false;
		
		ContextCachedItem ret = new ContextCachedItem();
		ret.bool = controller.canCreate(c,obj);
		((AaaContextImpl)c).setCached("ace_create"+ "|" + obj.getId(),ret);
		return ret.bool;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DbMetadata> T getObject(String type, UUID id) throws MException {
		DbSchemaService controller = getController(type);
		if (controller == null) return null;
		return (T) controller.getObject(type, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DbMetadata> T getObject(String type, String id) throws MException {
		DbSchemaService controller = getController(type);
		if (controller == null) return null;
		return (T) controller.getObject(type, id);
	}
	
	@Override
	public Set<Entry<String, DbSchemaService>> getController() {
		synchronized (controllers) {
			return controllers.entrySet();
		}
	}

	@Override
	public void onDelete(DbMetadata object) {

		if (object == null) return;
		
		ReferenceCollector collector = new ReferenceCollector() {
			LinkedList<UUID> list = new LinkedList<UUID>();
			@Override
			public void foundReference(Reference<?> ref) {
				if (ref.getType() == TYPE.CHILD) {
					if (ref.getObject() == null) return;
					// be sure not cause an infinity loop, a object should only be deleted once ...
					if (list.contains(ref.getObject().getId()))
						return;
					list.add(ref.getObject().getId());
					
					// delete the object and dependencies
					try {
						ref.doDelete();
					} catch (MException e) {
						log().w("deletion failed",ref.getObject(),ref.getObject().getClass(),e);
					}
				}
			}
		};
		
		collectRefereces(object, collector);
	}

	@Override
	public void collectRefereces(DbMetadata object, ReferenceCollector collector) {

		if (object == null) return;

		HashSet<DbSchemaService> distinct = new HashSet<DbSchemaService>();
		synchronized (controllers) {
			distinct.addAll(controllers.values());
		}

		for (DbSchemaService service : distinct)
			try {
				service.collectReferences(object, collector);
			} catch (Throwable t) {
				log().w(service.getClass(),object.getClass(),t);
			}
	}
	
	@Override
	public String processAdminSession() {
		
		String ticket = TicketUtil.ACCOUNT + TicketUtil.SEP + ROOT_CONTEXT.getAccountId() + TicketUtil.SEP + TicketUtil.SEP + TicketUtil.ADMIN;
		ContextPool.getInstance().set(ROOT_CONTEXT);
		
		return ticket;
	}

	@Override
	public boolean validatePassword(Account account, String password) {
		if (accountSource == null) return false;
		return account.validatePassword(password);
	}
	@Override
	public boolean canRead(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c.isAdminMode()) return true;
		return canRead(c, obj);
	}

	@Override
	public boolean canUpdate(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c.isAdminMode()) return true;
		return canUpdate(c, obj);
	}

	@Override
	public boolean canDelete(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c.isAdminMode()) return true;
		return canDelete(c, obj);
	}
	public boolean canCreate(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c == null || c.isAdminMode()) return true;
		return canCreate(c, obj);
	}
	
	public <T extends DbMetadata> T getObject(Class<T> type, UUID id) throws MException {
		return getObject(type.getCanonicalName(), id);
	}

	@Override
	public String createTrustTicket(AaaContext user) {
		if (trustSource == null) return null;
		return trustSource.createTrustTicket(user);
	}

	@aQute.bnd.annotation.component.Reference(optional=true,dynamic=true)
	public void setAccountSource(AccountSource source) {
		this.accountSource = source;
	}
	public void unsetAccountSource(AccountSource source) {
		this.accountSource = null;
	}
	
	@aQute.bnd.annotation.component.Reference(optional=true,dynamic=true)
	public void setTrustSource(TrustSource source) {
		this.trustSource = source;
	}
	
	public void unsetTrustSource(TrustSource source) {
		this.trustSource = null;
	}

	@aQute.bnd.annotation.component.Reference(optional=true,dynamic=true)
	public void setAuthorizationSource(AuthorizationSource source) {
		this.authorizationSource = source;
	}
	
	public void unsetAuthorizationSource(AuthorizationSource source) {
		this.authorizationSource = null;
	}

	@Override
	public boolean hasResourceAccess(Account account, String mappingName, String id, String action) {
		if (account == null || authorizationSource == null || mappingName == null ) return false;
		
		Boolean res = authorizationSource.hasResourceAccess(this, account,mappingName, id, action);
		if (res != null) return res;
		
		// action mapping
		if (action == null) return false;
		if (action.equals(Account.ACT_READ)) {
			res = authorizationSource.hasResourceAccess(this, account,mappingName, id, Account.ACT_MODIFY);
			if (res != null) return res;
		}
		
		return false;
	}
	
	@Override
	public boolean hasGroupAccess(Account account, String mapDef) {
		return hasGroupAccess(account, MCollection.toList( mapDef.split(",") ) );
	}
	
	@Override
	public boolean hasGroupAccess(Account account, List<String> mapDef) {
		for (String def : mapDef) {
			if (MString.isSet(def)) {
				def = def.trim().toLowerCase();
				if (def.equals("*")) return true;
				if (def.startsWith("user:") && def.substring(5).equals(account.getAccount().toLowerCase())) return true;
				if (def.startsWith("notuser:") && def.substring(8).equals(account.getAccount().toLowerCase())) return false;
				if (def.startsWith("not:") && account.hasGroup(def.substring(4))) return false;
				if (account.hasGroup(def)) return true;
			}
		}
		return false;
	}
	
}