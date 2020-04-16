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
package de.mhus.osgi.api.services;

import java.io.File;
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

import de.mhus.lib.annotations.pojo.Hidden;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.base.service.TimerImpl;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotFoundRuntimeException;

public class MOsgi {

    public static final String COMPONENT_NAME = "component.name";
    public static final String SERVICE_ID = "service.id";
    public static final String SERVICE_SCOPE = "service.scope";
    public static final String OBJECT_CLASS = "object.class";

    private static final Log log = Log.getLog(MOsgi.class);

    private static Timer localTimer; // fallback timer

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
            log.d(ifc, filter, e);
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
            log.d(ifc, filter, e);
        }
        return out;
    }

    /**
     * Use 
     * /@Reference
     * private TimerFactory timerFactory;
     * instead.
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

    public static String filterServiceId(String name) {
        return "(" + Constants.SERVICE_ID + "=" + name + ")";
    }

    public static String filterServiceName(String name) {
        return "(" + COMPONENT_NAME + "=" + name + ")";
    }

    public static String filterObjectClass(String clazz) {
        return "(" + Constants.OBJECTCLASS + "=" + clazz + ")";
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
            context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
        if (context == null)
            log.w(MSystem.currentStackTrace("BundleContext is empty"));
        return context;
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
            if (bundle.getSymbolicName().equals(name)) return bundle;
        throw new NotFoundException("Bundle not found", name);
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
            log.d("Touch configuration",pid,file);
            MFile.touch(file);
            return true;
        }
        return false;
    }

    public static String getPid(ComponentContext ctx) {
        return ctx.getComponentInstance().getInstance().getClass().getCanonicalName();
    }

    public static String getPid(Class<?> clazz) {
        return clazz.getCanonicalName();
    }

    public static Dictionary<String, Object> loadConfiguration(ComponentContext ctx) {
        return loadConfiguration(getPid(ctx));
    }

    public static Dictionary<String, Object> loadConfiguration(Class<?> serviceClass) {
        return loadConfiguration(findServicePid(serviceClass));
    }
    
    public static String findServicePid(Class<?> service) {
        if (service == null) throw new NullPointerException();
        {
            org.osgi.service.component.annotations.Component component = service.getAnnotation(org.osgi.service.component.annotations.Component.class);
            if (component != null) {
                if (component.configurationPid().length() > 0)
                    return component.configurationPid();
                if (component.name().length() > 0)
                    return component.name();
                return service.getCanonicalName();
            }
        }

        log.w("Class is not a service",service.getCanonicalName());
        return service.getCanonicalName();
    }

    public static Dictionary<String, Object> loadConfiguration(String pid) {
        try {
            touchConfig(pid);
            ConfigurationAdmin admin = M.l(ConfigurationAdmin.class);
            Configuration config = admin.getConfiguration(pid);
            if (config == null)
                config = admin.createFactoryConfiguration(pid);
            Dictionary<String, Object> prop = config.getProperties();
            if (prop == null) prop = new Hashtable<>();
            return prop;
        } catch (Throwable t) {
            log.d(pid,t);
        }
        return new Hashtable<>();
    }
    
    public static boolean saveConfiguration(ComponentContext ctx, Dictionary<String, Object> properties) {
        return saveConfiguration(getPid(ctx), properties);
    }
    
    public static boolean saveConfiguration(Class<?> serviceClass, Dictionary<String, Object> properties) {
        return saveConfiguration(findServicePid(serviceClass), properties);
    }
    
    public static boolean saveConfiguration(String pid, Dictionary<String, Object> properties) {
        try {
            touchConfig(pid); // Wait until config is loaded?!
            ConfigurationAdmin admin = M.l(ConfigurationAdmin.class);
            Configuration config = admin.getConfiguration(pid);
            if (config == null)
                config = admin.createFactoryConfiguration(pid);
            config.update(properties);
            return true;
        } catch (Throwable t) {
            log.d(pid,t);
        }
        return false;
    }
    

}
