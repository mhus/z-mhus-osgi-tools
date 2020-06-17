package de.mhus.osgi.services;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceDeactivate;
import de.mhus.lib.annotations.service.ServiceFactory;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MApi.SCOPE;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.cfg.CfgProvider;
import de.mhus.lib.core.config.ConfigList;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.services.IServiceManager;
import de.mhus.osgi.api.services.MOsgi;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;
import de.mhus.osgi.api.util.TemplateUtils;

@Component(immediate = true)
public class ServiceManager extends MLog implements IServiceManager {

    private static final String BLUEPRINT_PREFIX =  "service-";

    @Activate
    public void doActivate(ComponentContext ctx) {
        MOsgi.runAfterActivation(ctx, c -> doInit(c));
    }

    private void doInit(ComponentContext c) {
        
        x: while (true) {
            for ( Bundle b : c.getBundleContext().getBundles()) {
                int state = b.getState();
                if (state == Bundle.STARTING) 
                    continue x;
                break x;
            }
        }

        for (CfgProvider provider : MApi.get().getCfgManager().getProviders()) {
            ConfigList list = provider.getConfig().getArrayOrNull("service");
            if (list != null) {
                for (IConfig entry : list) {
                    try {
                        log().d("init",entry); // TODO update if needed
                        create(entry.getString("class", null), entry.getString("bundle", null));
                    } catch (Throwable t) {
                        log().e(entry,t);
                    }
                }
            }
        }
    }

    @Override
    public boolean create(String implClass, String bundleName) throws Exception {
        if (MString.isEmpty(bundleName)) return false;
        
        File outFile = getBlueprintFle(implClass);
        if (outFile.exists()) return false;
        
        // lookup class
        Class<?> clazz = null;
        if (MString.isSet(bundleName)) {
            Bundle bundle = MOsgi.getBundle(bundleName);
            clazz = bundle.loadClass(implClass);
        } else {
            OsgiBundleClassLoader cl = new OsgiBundleClassLoader();
            clazz = cl.loadClass(implClass);
        }
        
        ServiceComponent def = clazz.getAnnotation(ServiceComponent.class);
        if (def == null)
            throw new IllegalArgumentException("Implementing class is not annotated by ServiceComponent");
        
        // create bean
        
        StringBuilder bean = new StringBuilder();
        bean.append("<bean id=\"bean\" class=\"").append(clazz.getCanonicalName()).append("\" ");
        // activate
        {
            List<Method> list = MSystem.findMethodsWithAnnotation(clazz, ServiceActivate.class);
            if (list.size() > 0) {
                bean.append("init-method=\"").append(list.get(0).getName()).append("\" ");
            }
        }
        // deactivate
        {
            List<Method> list = MSystem.findMethodsWithAnnotation(clazz, ServiceDeactivate.class);
            if (list.size() > 0) {
                bean.append("destroy-method=\"").append(list.get(0).getName()).append("\" ");
            }
        }
        // factory
        {
            List<Method> list = MSystem.findMethodsWithAnnotation(clazz, ServiceFactory.class);
            if (list.size() > 0) {
                bean.append("factory-method=\"").append(list.get(0).getName()).append("\" ");
            }
        }
        if (def.eager()) {
            bean.append("activation=\"eager\" ");
        } else {
            bean.append("activation=\"lazy\" ");
        }
        if (def.singleton()) {
            bean.append("scope=\"singleton\" ");
        }
        bean.append(">\n");
        bean.append("</bean>\n");
        // create service
        
        StringBuilder service = new StringBuilder();
        
        Class<?>[] services = def.service();
        if (service.length() == 0) {
            services = clazz.getInterfaces();
        }
        if (service.length() == 0)
            throw new NotFoundException("interface for service not found");
        
        for (Class<?> srv : services) {
            service.append("<service interface=\"").append(srv.getCanonicalName()).append("\" ref=\"bean\">\n");
            service.append("  <service-properties>\n");
            service.append("    <entry key=\"osgi.jndi.service.name\" value=\"").append(srv.getCanonicalName()).append("\"/>\n");
            service.append("    <entry key=\"de.mhus.osgi.services.managed\" value=\"true\"/>\n");
            // service.append("    <entry key=\"").append(MOsgi.OBJECT_CLASS).append("\" value=\"").append(srv.getCanonicalName()).append("\"/>\n");

            String name = def.name();
            if (MString.isEmpty(name)) name = srv.getCanonicalName();
            service.append("    <entry key=\"").append(MOsgi.COMPONENT_NAME).append("\" value=\"").append(MXml.encode(name)).append("\"/>\n");
            
            String[] pid = def.configurationPid();
            if (pid.length == 0) pid = new String[] {srv.getCanonicalName()};
            service.append("    <entry key=\"").append(MOsgi.SERVICE_PID).append("\" value=\"").append(MXml.encode(MString.join(pid,','))).append("\"/>\n");
            
            for (String prop : def.property()) {
                String k = MString.beforeIndex(prop, '=');
                String v = MString.afterIndex(prop, '=');
                service.append("    <entry key=\"").append(MXml.encode(k)).append("\" value=\"").append(MXml.encode(v)).append("\"/>\n");
            }
            service.append("  </service-properties>\n");
            service.append("</service>\n");
        }
        
        // print template
        
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("bean", bean.toString());
        properties.put("service", service.toString());
        
        String templateFile = "blueprint.xml";
        InputStream is = this.getClass().getResourceAsStream(templateFile);
        if (is == null) {
            throw new IllegalArgumentException(
                    "Template resource " + templateFile + " doesn't exist");
        }
        TemplateUtils.createFromTemplate(outFile, is, properties);

        return true;
    }

    private File getBlueprintFle(String implClass) {
        return MApi.getFile(SCOPE.DEPLOY, MFile.normalize(BLUEPRINT_PREFIX + implClass.toLowerCase()) + ".xml");
    }

    @Override
    public boolean delete(String implClass) {
        File outFile = getBlueprintFle(implClass);
        if (!outFile.exists()) return false;
        outFile.delete();
        return true;
    }

    @Override
    public List<String> list() {
        LinkedList<String> out = new LinkedList<>();
        for (File f : MApi.getFile(SCOPE.DEPLOY, ".").listFiles())
            if (f.isFile() && f.getName().startsWith(BLUEPRINT_PREFIX) && f.getName().endsWith(".xml")) {
                out.add(f.getName().substring(BLUEPRINT_PREFIX.length(), f.getName().length() - 4));
            }
        return out;
    }

}
