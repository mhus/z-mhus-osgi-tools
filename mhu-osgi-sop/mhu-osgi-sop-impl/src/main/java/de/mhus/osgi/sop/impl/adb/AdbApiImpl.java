package de.mhus.osgi.sop.impl.adb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.ContextCachedItem;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.Reference;
import de.mhus.osgi.sop.api.adb.Reference.TYPE;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.model.ActionTask;
import de.mhus.osgi.sop.api.model.ObjectParameter;
import de.mhus.osgi.sop.impl.AaaContextImpl;

public class AdbApiImpl extends MLog implements AdbApi {

	private HashMap<String, DbSchemaService> controllers = new HashMap<String, DbSchemaService>();
	private ServiceTracker<DbSchemaService,DbSchemaService> accessTracker;
	private BundleContext context;

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		accessTracker = new ServiceTracker<>(context, DbSchemaService.class, new MyAccessTrackerCustomizer() );
		accessTracker.open();
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

		DbManager manager = getManager();	

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
		DbManager manager = getManager();	
		
		DbCollection<ActionTask> res = manager.getByQualification(Db.query(ActionTask.class).eq(Db.attr("queue"), Db.value(queue)));
		for (ActionTask task : res) {
			out.add(task);
			if (max > 0 && out.size() >= max) break;
		}
		res.close();
		return out;
	}

	@Override
	public List<ObjectParameter> getParameters(Class<?> type, UUID id) throws MException {
		return getParameters(type.getCanonicalName(), id);
	}
	
	@Override
	public List<ObjectParameter> getParameters(String type, UUID id) throws MException {
		
		
		DbManager manager = getManager();
		List<ObjectParameter> out = manager.getByQualification(
				Db.query(ObjectParameter.class)
				.eq(Db.attr("objecttype"), Db.value(type))
				.eq(Db.attr("objectid"),Db.value(id))
				).toCacheAndClose();
		
		return out;
	}

	@Override
	public void setGlobalParameter(String key, String value) throws MException {
		setParameter(ObjectParameter.TYPE_GLOBAL, MConstants.EMPTY_UUID, key, value);
	}

	@Override
	public void setParameter(Class<?> type, UUID id, String key, String value) throws MException {
		setParameter(type.getCanonicalName(), id, key, value);
	}
	
	@Override
	public void setParameter(String type, UUID id, String key, String value) throws MException {
				
		ObjectParameter out = getParameter(type, id, key);
		if (out == null) {
			DbManager manager = getManager();
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
	
	@Override
	public ObjectParameter getGlobalParameter(String key) throws MException {
		return getParameter(ObjectParameter.TYPE_GLOBAL, MConstants.EMPTY_UUID, key);
	}
	
	@Override
	public String getValue(Class<?> type, UUID id, String key, String def) throws MException {
		return getValue(type.getCanonicalName(), id, key, def);
	}
	
	@Override
	public String getValue(String type, UUID id, String key, String def) throws MException {
		ObjectParameter p = getParameter(type, id, key);
		if (p == null || p.getValue() == null) return def;
		return p.getValue();
	}
	
	@Override
	public ObjectParameter getParameter(Class<?> type, UUID id, String key) throws MException {
		return getParameter(type.getCanonicalName(), id, key);
	}
	
	@Override
	public ObjectParameter getParameter(String type, UUID id, String key) throws MException {
		
		DbManager manager = getManager();
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
		DbManager manager = getManager();
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

	@Override
	public DbManager getManager() {
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
		DbCollection<T> res = getManager().getByQualification(query);
		if (!res.skip(page * PAGE_SIZE)) return list;
		while (res.hasNext()) {
			list.add(res.next());
			if (list.size() >= PAGE_SIZE) break;
		}
		res.close();
		return list;
	}

	@Override
	public List<ActionTask> getActionTaskPage(String queue, int size) {
		
		LinkedList<ActionTask> out = new LinkedList<ActionTask>();
		try {
			DbCollection<ActionTask> res = getManager().getByQualification(Db.query(ActionTask.class).eq("queue", queue).desc("creationdate"));
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
						doDelete(ref);
					} catch (MException e) {
						log().w("deletion failed",ref.getObject(),ref.getObject().getClass(),e);
					}
				}
			}
		};
		
		collectRefereces(object, collector);
	}

	protected void doDelete(Reference<?> ref) throws MException {
		log().d("start delete",ref.getObject(),ref.getType());
		onDelete(ref.getObject());
		log().d("delete",ref);
		ref.getObject().delete();
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
	public boolean canRead(DbMetadata obj) throws MException {
		if (obj == null) return false;
		AaaContext c = getCurrent();
		if (c.isAdminMode()) return true;
		return canRead(c, obj);
	}

	private AaaContext getCurrent() {
		return MApi.lookup(AccessApi.class).getCurrentOrGuest();
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


}
