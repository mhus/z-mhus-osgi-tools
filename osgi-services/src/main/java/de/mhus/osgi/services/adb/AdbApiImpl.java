/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.services.adb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.basics.UuidIdentificable;
import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.api.aaa.ContextCachedItem;
import de.mhus.osgi.api.adb.AdbApi;
import de.mhus.osgi.api.adb.DbSchemaService;
import de.mhus.osgi.api.adb.Reference;
import de.mhus.osgi.api.adb.Reference.TYPE;
import de.mhus.osgi.api.adb.ReferenceCollector;

// @Component(immediate=true) done by blueprint
public class AdbApiImpl extends MLog implements AdbApi {

    private HashMap<String, DbSchemaService> controllers = new HashMap<String, DbSchemaService>();
    private ServiceTracker<DbSchemaService, DbSchemaService> accessTracker;
    private BundleContext context;

    //	@Activate
    public void doActivate() {
        BundleContext context = FrameworkUtil.getBundle(AdbApiImpl.class).getBundleContext();
        doActivate(context);
    }

    public void doActivate(BundleContext context) {
        this.context = context;
        init();
    }

    private void init() {
        if (accessTracker == null) {
            if (context == null)
                context = FrameworkUtil.getBundle(AdbApiImpl.class).getBundleContext();
            if (context != null) {
                accessTracker =
                        new ServiceTracker<>(
                                context, DbSchemaService.class, new MyAccessTrackerCustomizer());
                accessTracker.open();
            }
        }
    }
    //	@Deactivate
    public void doDeactivate() {
        if (accessTracker != null) accessTracker.close();
        accessTracker = null;
        context = null;
        if (controllers != null) controllers.clear();
    }

    @Override
    public XdbService getManager() {
        init();
        return SopDbManagerService.instance().getManager();
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
    public DbSchemaService getController(String type) throws MException {
        init();
        if (type == null) throw new MException("type is null");
        DbSchemaService ret = controllers.get(type);
        if (ret == null) throw new MException("Access Controller not found", type);
        return ret;
    }

    protected boolean canReadX(DbMetadata obj) throws MException {

//XXX        Boolean item = ((AaaContextImpl) c).getCached("ace_read|" + obj.getId());
//        if (item != null) return item;

        DbSchemaService controller = getController(obj.getClass().getCanonicalName());
        if (controller == null) return false;

        ContextCachedItem ret = new ContextCachedItem();
        ret.bool = controller.canRead(obj);
//        ((AaaContextImpl) c)
//                .setCached("ace_read|" + obj.getId(), MPeriod.MINUTE_IN_MILLISECOUNDS * 5, ret);
        return ret.bool;
    }

    protected boolean canUpdateX(DbMetadata obj) throws MException {

//        Boolean item = ((AaaContextImpl) c).getCached("ace_update|" + obj.getId());
//        if (item != null) return item;

        DbSchemaService controller = getController(obj.getClass().getCanonicalName());
        if (controller == null) return false;

        ContextCachedItem ret = new ContextCachedItem();
        ret.bool = controller.canUpdate(obj);
//        ((AaaContextImpl) c)
//               .setCached("ace_update|" + obj.getId(), MPeriod.MINUTE_IN_MILLISECOUNDS * 5, ret);
        return ret.bool;
    }

    protected boolean canDeleteX(DbMetadata obj) throws MException {

//        Boolean item = ((AaaContextImpl) c).getCached("ace_delete" + "|" + obj.getId());
//        if (item != null) return item;

        DbSchemaService controller = getController(obj.getClass().getCanonicalName());
        if (controller == null) return false;

        ContextCachedItem ret = new ContextCachedItem();
        ret.bool = controller.canDelete(obj);
//        ((AaaContextImpl) c)
//                .setCached("ace_delete|" + obj.getId(), MPeriod.MINUTE_IN_MILLISECOUNDS * 5, ret);
        return ret.bool;
    }

    protected boolean canCreateX(DbMetadata obj) throws MException {
//        Boolean item = ((AaaContextImpl) c).getCached("ace_create" + "|" + obj.getId());
//        if (item != null) return item;

        DbSchemaService controller = getController(obj.getClass().getCanonicalName());
        if (controller == null) return false;

        ContextCachedItem ret = new ContextCachedItem();
        ret.bool = controller.canCreate(obj);
//        ((AaaContextImpl) c)
//                .setCached("ace_create|" + obj.getId(), MPeriod.MINUTE_IN_MILLISECOUNDS * 5, ret);
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
    public void onDelete(Persistable object) {

        if (object == null) return;

        ReferenceCollector collector =
                new ReferenceCollector() {
                    LinkedList<UUID> list = new LinkedList<UUID>();

                    @Override
                    public void foundReference(Reference<?> ref) {
                        if (ref.getType() == TYPE.CHILD) {
                            if (ref.getObject() == null) return;
                            // be sure not cause an infinity loop, a object should only be deleted
                            // once ...
                            if (ref.getObject() instanceof UuidIdentificable) {
                                if (list.contains(((UuidIdentificable) ref.getObject()).getId()))
                                    return;
                                list.add(((UuidIdentificable) ref.getObject()).getId());
                            }
                            // delete the object and dependencies
                            try {
                                doDelete(ref);
                            } catch (MException e) {
                                log().w(
                                                "deletion failed",
                                                ref.getObject(),
                                                ref.getObject().getClass(),
                                                e);
                            }
                        }
                    }
                };

        collectRefereces(object, collector);
    }

    protected void doDelete(Reference<?> ref) throws MException {
        log().d("start delete", ref.getObject(), ref.getType());
        onDelete(ref.getObject());
        log().d("delete", ref);
        getManager().delete(ref.getObject());
    }

    @Override
    public void collectRefereces(Persistable object, ReferenceCollector collector) {

        if (object == null) return;

        HashSet<DbSchemaService> distinct = new HashSet<DbSchemaService>();
        synchronized (controllers) {
            distinct.addAll(controllers.values());
        }

        for (DbSchemaService service : distinct)
            try {
                service.collectReferences(object, collector);
            } catch (Throwable t) {
                log().w(service.getClass(), object.getClass(), t);
            }
    }

    @Override
    public boolean canRead(DbMetadata obj) throws MException {
        if (obj == null) return false;
        return canReadX(obj);
    }

    @Override
    public boolean canUpdate(DbMetadata obj) throws MException {
        if (obj == null) return false;
        return canUpdateX(obj);
    }

    @Override
    public boolean canDelete(DbMetadata obj) throws MException {
        if (obj == null) return false;
        return canDeleteX(obj);
    }

    @Override
    public boolean canCreate(DbMetadata obj) throws MException {
        if (obj == null) return false;
        return canCreateX(obj);
    }

    @Override
    public <T extends DbMetadata> T getObject(Class<T> type, UUID id) throws MException {
        return getObject(type.getCanonicalName(), id);
    }

    private class MyAccessTrackerCustomizer
            implements ServiceTrackerCustomizer<DbSchemaService, DbSchemaService> {

        @Override
        public DbSchemaService addingService(ServiceReference<DbSchemaService> reference) {

            DbSchemaService service = context.getService(reference);
            if (service != null) {
                LinkedList<Class<? extends Persistable>> list = new LinkedList<>();
                service.registerObjectTypes(list);
                synchronized (controllers) {
                    for (Class<?> clazz : list) {
                        log().i(
                                        "register access controller",
                                        clazz,
                                        service.getClass().getCanonicalName());
                        DbSchemaService last = controllers.put(clazz.getCanonicalName(), service);
                        if (last != null)
                            log().w(
                                            "overwrote access controller",
                                            clazz,
                                            service.getClass().getCanonicalName());
                    }
                }
            }
            return service;
        }

        @Override
        public void modifiedService(
                ServiceReference<DbSchemaService> reference, DbSchemaService service) {}

        @Override
        public void removedService(
                ServiceReference<DbSchemaService> reference, DbSchemaService service) {

            if (service != null) {
                LinkedList<Class<? extends Persistable>> list = new LinkedList<>();
                service.registerObjectTypes(list);
                synchronized (controllers) {
                    for (Class<?> clazz : list) {
                        log().i(
                                        "remove access controller",
                                        clazz,
                                        service.getClass().getCanonicalName());
                        controllers.remove(clazz.getCanonicalName());
                    }
                }
            }
        }
    }
}
