/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.annotations.pojo.Hidden;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.service.TimerFactory;
import de.mhus.lib.core.service.TimerIfc;
import de.mhus.lib.core.service.TimerImpl;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotFoundRuntimeException;
import de.mhus.osgi.api.services.BundleStarter;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

public class MOsgi {

    public static final String COMPONENT_NAME = "component.name";
    public static final String SERVICE_ID = "service.id";
    public static final String SERVICE_SCOPE = "service.scope";
    public static final Object SERVICE_PID = "service.pid";
    public static final String OBJECT_CLASS = "object.class";

    private static final Log log = Log.getLog(MOsgi.class);

    private static Timer localTimer; // fallback timer

    public static <T> T getService(ServiceReference<T> reference) {
        BundleContext context = getBundleContext();
        return context.getService(reference);
    }

    public static <T> T getService(Class<T> ifc) throws NotFoundException {
        BundleContext context = FrameworkUtil.getBundle(ifc).getBundleContext();
        if (context == null) context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
        if (context == null) throw new NotFoundException("service context not found", ifc);
        ServiceReference<T> ref = context.getServiceReference(ifc);
        if (ref == null) throw new NotFoundException("service reference not found", ifc);
        T obj = context.getService(ref);
        if (obj == null) throw new NotFoundException("service not found", ifc);
        return obj;
    }

    public static <T> T getServiceOrNull(Class<T> ifc) {
        Bundle bundle = FrameworkUtil.getBundle(ifc);
        BundleContext context = null;
        if (bundle != null) context = bundle.getBundleContext();
        if (context == null) {
            bundle = FrameworkUtil.getBundle(MOsgi.class);
            if (bundle != null) context = bundle.getBundleContext();
        }
        if (context == null) return null;
        ServiceReference<T> ref = context.getServiceReference(ifc);
        if (ref == null) return null;
        T obj = context.getService(ref);
        if (obj == null) return null;
        return obj;
    }

    public static <T> T getService(Class<T> ifc, String filter) throws NotFoundException {
        List<T> list = getServices(ifc, filter);
        if (list.size() == 0) throw new NotFoundException("service not found", ifc, filter);
        return list.get(0);
    }

    public static <T> List<T> getServices(Class<T> ifc, String filter) {
        BundleContext context = FrameworkUtil.getBundle(ifc).getBundleContext();
        if (context == null) context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
        if (context == null) throw new NotFoundRuntimeException("service context not found", ifc);
        LinkedList<T> out = new LinkedList<>();
        try {
            for (ServiceReference<T> ref : context.getServiceReferences(ifc, filter)) {
                T obj = context.getService(ref);
                out.add(obj);
            }
        } catch (Exception e) {
            log.d("get service failed", ifc, filter, e);
        }
        return out;
    }

    public static <T> List<Service<T>> getServiceRefs(Class<T> ifc, String filter) {
        BundleContext context = FrameworkUtil.getBundle(ifc).getBundleContext();
        if (context == null) context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
        if (context == null) throw new NotFoundRuntimeException("service context not found", ifc);
        LinkedList<Service<T>> out = new LinkedList<>();
        try {
            for (ServiceReference<T> ref : context.getServiceReferences(ifc, filter)) {
                out.add(new Service<T>(ref, context));
            }
        } catch (Exception e) {
            log.d("get service failed", ifc, filter, e);
        }
        return out;
    }

    /**
     * Use /@Reference private TimerFactory timerFactory; instead.
     *
     * @return A timer factory
     */
    @Deprecated
    public static synchronized TimerIfc getTimer() {
        TimerIfc timer = null;
        try {
            timer = getService(TimerFactory.class).getTimer();
        } catch (Throwable t) {
        }
        if (timer == null) {
            // oh oh
            if (localTimer == null) localTimer = new Timer("de.mhu.lib.localtimer", true);
            timer = new TimerImpl(localTimer);
        }
        return timer;
    }

    public static String filterValue(String name, String value) {
        return "(" + name + "=" + value + ")";
    }

    public static String filterServiceId(String name) {
        return "(" + Constants.SERVICE_ID + "=" + name + ")";
    }

    public static String filterServiceName(String name) {
        return "(" + COMPONENT_NAME + "=" + name + ")";
    }

    public static String filterObjectClass(String clazz) {
        return "(" + Constants.OBJECTCLASS + "=" + clazz + ")";
    }

    public static String filterObjectClass(Class<?> clazz) {
        return "(" + Constants.OBJECTCLASS + "=" + clazz.getCanonicalName() + ")";
    }

    public static String filterAdd(String... parts) {
        StringBuilder out = new StringBuilder().append("(&");
        for (String part : parts) out.append(part);
        out.append(")");
        return out.toString();
    }

    public static String getServiceId(ServiceReference<?> ref) {
        if (ref == null) return null;
        return String.valueOf(ref.getProperty(Constants.SERVICE_ID));
    }

    public static String getServiceName(ServiceReference<?> ref) {
        if (ref == null) return null;
        return String.valueOf(ref.getProperty(COMPONENT_NAME));
    }

    public static class Service<T> {

        private ServiceReference<T> ref;
        private T obj;
        private BundleContext context;

        public Service(ServiceReference<T> ref, BundleContext context) {
            this.ref = ref;
            this.obj = null;
            this.context = context;
        }

        public T getService() {
            if (obj == null) obj = context.getService(ref);
            return obj;
        }

        public ServiceReference<T> getReference() {
            return ref;
        }

        public String getName() {
            return MOsgi.getServiceName(ref);
        }
    }

    /**
     * This function returns in every case a valid bundle context. It's the context of a base
     * bundle, not the context of the current working bundle. Use the context to access services in
     * every case.
     *
     * @return BundleContext
     */
    public static BundleContext getBundleContext() {
        BundleContext context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
        if (context == null)
            context = FrameworkUtil.getBundle(FrameworkUtil.class).getBundleContext();
        if (context == null) log.w(MSystem.currentStackTrace("BundleContext is empty"));
        return context;
    }

    public static BundleContext getBundleContext(Class<?> clazz) {
        return FrameworkUtil.getBundle(clazz).getBundleContext();
    }

    public enum BUNDLE_STATE {
        UNINSTALLED,
        INSTALLED,
        RESOLVED,
        STARTING,
        STOPPING,
        ACTIVE,
        UNKNOWN
    }

    public static BUNDLE_STATE getState(Bundle bundle) {
        int state = bundle.getState();
        switch (state) {
            case Bundle.UNINSTALLED:
                return BUNDLE_STATE.UNINSTALLED;
            case Bundle.INSTALLED:
                return BUNDLE_STATE.INSTALLED;
            case Bundle.RESOLVED:
                return BUNDLE_STATE.RESOLVED;
            case Bundle.STARTING:
                return BUNDLE_STATE.STARTING;
            case Bundle.STOPPING:
                return BUNDLE_STATE.STOPPING;
            case Bundle.ACTIVE:
                return BUNDLE_STATE.ACTIVE;
            default:
                return BUNDLE_STATE.UNKNOWN;
        }
    }

    public static Version getBundelVersion(Class<?> owner) {
        Bundle bundle = FrameworkUtil.getBundle(owner);
        if (bundle == null) return Version.V_0_0_0;
        return new Version(bundle.getVersion().toString());
    }

    public static File getTmpFolder() {
        File dir = new File("data/tmp");
        if (dir.exists()) return dir;
        return new File(MSystem.getTmpDirectory());
    }

    /**
     * Return the bundle with the given name or throw NotFoundException
     *
     * @param name
     * @return The Bundle
     * @throws NotFoundException
     */
    public static Bundle getBundle(String name) throws NotFoundException {
        for (Bundle bundle : FrameworkUtil.getBundle(MOsgi.class).getBundleContext().getBundles())
            if (bundle.getSymbolicName().equals(name)
                    || name.equals(String.valueOf(bundle.getBundleId()))) return bundle;
        throw new NotFoundException("Bundle not found", name);
    }

    public static Bundle getBundleOrNull(long bundleId) {
        Bundle bundle = FrameworkUtil.getBundle(org.apache.karaf.log.core.LogMBean.class);
        if (bundle == null) return null;
        try {
            BundleContext context = bundle.getBundleContext();
            if (context == null) return null;
            return context.getBundle(bundleId);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static void runAfterActivation(
            ComponentContext ctx, Consumer<ComponentContext> consumer) {

        if (consumer == null) return;

        runAfterActivation(
                ctx,
                new BundleStarter() {
                    @Hidden private Log log;
                    // get log name from consumer
                    // de.mhus.inka.tryit.ConsumerTest$$Lambda$1/0x0000000800060840@75828a0f
                    @Override
                    public synchronized Log log() {
                        if (log == null) {
                            String name = consumer.toString();
                            name = MString.beforeIndex(name, '$');
                            log = MApi.get().lookupLog(name);
                        }
                        return log;
                    }

                    @Override
                    public void run() {
                        consumer.accept(ctx);
                    }
                });
    }

    public void listenForServiceEvents(
            ComponentContext ctx, Class<?> service, Consumer<Event> consumer) {
        //      EVENT: org/osgi/framework/ServiceEvent/REGISTERED
        // {event=org.osgi.framework.ServiceEvent[source=[javax.sql.DataSource]],
        // event.topics=org/osgi/framework/ServiceEvent/REGISTERED, service=[javax.sql.DataSource],
        // service.id=265, service.objectClass=[javax.sql.DataSource], timestamp=1593221077477}
        //      EVENT: org/osgi/framework/ServiceEvent/UNREGISTERING
        // {event=org.osgi.framework.ServiceEvent[source=[javax.sql.DataSource]],
        // event.topics=org/osgi/framework/ServiceEvent/UNREGISTERING,
        // service=[javax.sql.DataSource], service.id=261,
        // service.objectClass=[javax.sql.DataSource], timestamp=1593221069398}
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(EventConstants.EVENT_TOPIC, new String[] {"org/osgi/framework/ServiceEvent/*"});
        EventHandler inst =
                new EventHandler() {
                    @Override
                    public void handleEvent(Event event) {
                        consumer.accept(event);
                    }
                };
        ctx.getBundleContext().registerService(EventHandler.class, inst, props);
    }

    public static void runAfterActivation(ComponentContext ctx, BundleStarter task) {

        if (ctx == null || task == null) return;
        Bundle bundle = ctx.getUsingBundle();

        new MThread(
                        new Runnable() {

                            @Override
                            public void run() {
                                if (bundle == null) {
                                    task.log().i("executing bundle is null");
                                    // can't wait for end of activation
                                    MThread.sleep(2000);
                                } else {
                                    // wait for end of activation
                                    long start = System.currentTimeMillis();
                                    while (true) {
                                        MThread.sleep(300);
                                        int state = bundle.getState();
                                        if (state == Bundle.STOPPING
                                                || state == Bundle.UNINSTALLED) {
                                            task.log().i("activation terminated");
                                            return;
                                        }
                                        if (state == Bundle.ACTIVE) break;
                                        if (MPeriod.isTimeOut(start, task.getTimeout())) {
                                            task.log().i("activation timeout");
                                            if (task.exitOnTimeout()) return;
                                            else break;
                                        }
                                    }
                                }

                                try {
                                    task.log().d("start");
                                    task.run();
                                    while (task.isRetry()) {
                                        MThread.sleep(2000);
                                        task.log().d("retry");
                                        task.run();
                                    }
                                } catch (Throwable t) {
                                    task.log().e(t);
                                }
                            }
                        },
                        "Starter:" + getBundleCaption(ctx.getUsingBundle()))
                .start();
    }

    /**
     * Return bundle name and id in brackets
     *
     * @param bundle
     * @return Caption to identify the bundle
     */
    public static String getBundleCaption(Bundle bundle) {
        if (bundle == null) return "null";
        return bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]";
    }

    public static boolean touchConfig(Class<?> serviceClass) {
        return touchConfig(findServicePid(serviceClass));
    }

    public static boolean touchConfig(String pid) {
        File file = MApi.getFile(MApi.SCOPE.ETC, pid + ".cfg");
        if (!file.exists()) {
            // log.d("Touch configuration",pid,file);
            MFile.touch(file);
            return true;
        }
        return false;
    }

    public static String getPid(ComponentContext ctx) {
        return findServicePid(ctx.getComponentInstance().getInstance().getClass());
    }

    public static String getPid(Class<?> clazz) {
        return findServicePid(clazz);
    }

    public static Dictionary<String, Object> loadConfiguration(
            ConfigurationAdmin admin, ComponentContext ctx) {
        return loadConfiguration(admin, getPid(ctx));
    }

    public static Dictionary<String, Object> loadConfiguration(
            ConfigurationAdmin admin, Class<?> serviceClass) {
        return loadConfiguration(admin, findServicePid(serviceClass));
    }

    public static String findServicePid(Class<?> service) {
        if (service == null) throw new NullPointerException();
        if (service.isInterface()) return service.getCanonicalName();

        try {
            org.osgi.service.component.annotations.Component component =
                    service.getAnnotation(org.osgi.service.component.annotations.Component.class);
            if (component != null) {
                if (component.configurationPid().length > 0) {
                    if (!component.configurationPid()[0].equals(Component.NAME))
                        return component.configurationPid()[0];
                }
                if (component.name().length() > 0) return component.name();
                if (component.service().length > 0)
                    return component.service()[0].getCanonicalName();
            }
        } catch (Throwable e) {
            // will create a loop log.d(service,e);
        }
        if (service.getInterfaces().length > 0) {
            return service.getInterfaces()[0].getCanonicalName();
        }

        if (log != null) log.w("Class is not a service", service.getCanonicalName());
        return service.getCanonicalName();
    }

    public static Dictionary<String, Object> loadConfiguration(
            ConfigurationAdmin admin, String pid) {
        try {
            touchConfig(pid);
            // ConfigurationAdmin admin = M.l(ConfigurationAdmin.class);
            Configuration config = admin.getConfiguration(pid);
            if (config == null) config = admin.createFactoryConfiguration(pid);
            Dictionary<String, Object> prop = config.getProperties();
            if (prop == null) prop = new Hashtable<>();
            return prop;
        } catch (Throwable t) {
            log.w(pid, t);
        }
        return new Hashtable<>();
    }

    public static boolean saveConfiguration(
            ConfigurationAdmin admin, ComponentContext ctx, Dictionary<String, Object> properties) {
        return saveConfiguration(admin, getPid(ctx), properties);
    }

    public static boolean saveConfiguration(
            ConfigurationAdmin admin,
            Class<?> serviceClass,
            Dictionary<String, Object> properties) {
        return saveConfiguration(admin, findServicePid(serviceClass), properties);
    }

    public static boolean saveConfiguration(
            ConfigurationAdmin admin, String pid, Dictionary<String, Object> properties) {
        try {
            touchConfig(pid); // Wait until config is loaded?!
            // ConfigurationAdmin admin = M.l(ConfigurationAdmin.class);
            Configuration config = admin.getConfiguration(pid);
            if (config == null) config = admin.createFactoryConfiguration(pid);
            config.update(properties);
            return true;
        } catch (Throwable t) {
            log.w(pid, t);
        }
        return false;
    }

    /**
     * Compile a list of properties from the input parameters. Input parameters are alternating
     * key-value values.
     *
     * @param kv key, value, key, value, ...
     * @return The property dictionary
     */
    public static Dictionary<String, ?> createProperties(Object... kv) {
        Hashtable<String, Object> prop = new Hashtable<>();
        for (int i = 0; i < kv.length - 1; i = i + 2) {
            prop.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return prop;
    }

    public static <T> List<String> collectStringProperty(
            List<Service<T>> serviceRefs, String propertyName) {
        ArrayList<String> out = new ArrayList<>(serviceRefs.size());
        for (Service<?> ref : serviceRefs) {
            Object value = ref.getReference().getProperty(propertyName);
            if (value != null) out.add(String.valueOf(value));
        }
        return out;
    }

    public static <T> List<Object> collectProperty(
            List<Service<T>> serviceRefs, String propertyName) {
        ArrayList<Object> out = new ArrayList<>(serviceRefs.size());
        for (Service<?> ref : serviceRefs) {
            Object value = ref.getReference().getProperty(propertyName);
            if (value != null) out.add(value);
        }
        return null;
    }

    public static boolean isValid(BundleContext bundleContext) {
        return bundleContext != null && bundleContext.getBundle().getState() != Bundle.UNINSTALLED;
    }

    /**
     * Load a accessible class from the osgi environment. Will use the first if more then one is
     * available.
     *
     * @param name
     * @return The class
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(String name) throws ClassNotFoundException {
        return new OsgiBundleClassLoader().loadClass(name); // cache??!!
    }
}
