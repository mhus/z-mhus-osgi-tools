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
package de.mhus.osgi.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceDeactivate;
import de.mhus.lib.annotations.service.ServiceFactory;
import de.mhus.lib.annotations.service.ServiceReference;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MApi.SCOPE;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.mapi.MCfgManager;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.services.IBlueprintManager;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;
import de.mhus.osgi.api.util.TemplateUtils;

// https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.blueprint.html

@Component(immediate = true)
public class BlueprintManager extends MLog implements IBlueprintManager {

    private static final String BLUEPRINT_PREFIX = "service-";

    @Activate
    public void doActivate(ComponentContext ctx) {
        MOsgi.runAfterActivation(ctx, c -> doInit(c));
    }

    @Override
    public void reloadConfigured() {
        log().i("reloadConfigured");
        List<INode> list = MCfgManager.getGlobalConfigurations("service");
        for (INode entry : list) {
            try {
                log().i("create/update", entry);
                create(entry.getString("class", null), entry.getString("bundle", null), true);
            } catch (Throwable t) {
                log().e("create blueprint failed", entry, t);
            }
        }
    }

    private void doInit(ComponentContext c) {
        x:
        while (true) {
            for (Bundle b : c.getBundleContext().getBundles()) {
                int state = b.getState();
                if (state == Bundle.STARTING) {
                    MThread.sleep(200);
                    continue x;
                }
            }
            break;
        }
        reloadConfigured();
    }

    @Override
    public boolean create(String implClass, String bundleName) throws Exception {
        return create(implClass, bundleName, false);
    }

    @Override
    public boolean update(String implClass, String bundleName) throws Exception {
        return create(implClass, bundleName, true);
    }

    public boolean create(String implClass, String bundleName, boolean update) throws Exception {
        if (MString.isEmpty(implClass)) return false;
        File outFile = getBlueprintFle(implClass);

        String newContent = toString(implClass, bundleName);

        if (outFile.exists()) {
            if (!update) return false;

            String currentContent = MFile.readFile(outFile);
            if (newContent.equals(currentContent)) return false;
        }

        MFile.writeFile(outFile, newContent);
        return true;
    }

    @Override
    public String test(String implClass, String bundleName) throws Exception {
        return toString(implClass, bundleName);
    }

    public String toString(String implClass, String bundleName) throws Exception {

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
            throw new IllegalArgumentException(
                    "Implementing class is not annotated by ServiceComponent");

        // create references

        StringBuilder references = new StringBuilder();
        StringBuilder beanReferences = new StringBuilder();

        { // methods
            List<Method> list = MSystem.findMethodsWithAnnotation(clazz, ServiceReference.class);

            // find unset methods
            HashMap<String, Method> unsetMethods = new HashMap<>();
            for (Method method : list) {
                ServiceReference rDef = method.getAnnotation(ServiceReference.class);
                if (rDef == null || !rDef.unset()) continue;
                String rIfc = method.getParameterTypes()[0].getCanonicalName();
                if (rDef.service() != Object.class) rIfc = rDef.service().getCanonicalName();

                if (method.getParameterCount() < 1 || method.getParameterCount() > 2)
                    throw new IllegalArgumentException(
                            "Reference has wrong parameter count: " + method);

                unsetMethods.put(rIfc, method);
            }

            // find setter
            for (Method method : list) {
                if (method.getParameterCount() < 1 || method.getParameterCount() > 2)
                    throw new IllegalArgumentException(
                            "Reference has wrong parameter count: " + method);

                String rName = "refm-" + method.getName();
                ServiceReference rDef = method.getAnnotation(ServiceReference.class);

                if (rDef.unset()) continue;

                String rIfc = method.getParameterTypes()[0].getCanonicalName();
                if (rDef.service() != Object.class) rIfc = rDef.service().getCanonicalName();

                String rField = method.getName();
                if (rField.startsWith("set") && rField.length() > 3) {
                    rField = rField.substring(3, 4).toLowerCase() + rField.substring(4);
                }
                if (rIfc.equals(BlueprintContainer.class.getCanonicalName())) {
                    // special for bundle context
                    beanReferences
                            .append("  <property name=\"")
                            .append(rField)
                            .append("\" ref=\"blueprintContainer\"/>\n");
                } else if (rIfc.equals(Bundle.class.getCanonicalName())) {
                    // special for bundle context
                    beanReferences
                            .append("  <property name=\"")
                            .append(rField)
                            .append("\" ref=\"blueprintBundle\"/>\n");
                } else if (rIfc.equals(BundleContext.class.getCanonicalName())) {
                    // special for bundle context
                    beanReferences
                            .append("  <property name=\"")
                            .append(rField)
                            .append("\" ref=\"blueprintBundleContext\"/>\n");
                } else {
                    String rMethod = method.getName();
                    references
                            .append("<reference id=\"")
                            .append(rName)
                            .append("\" interface=\"")
                            .append(rIfc)
                            .append("\" ");
                    if (rDef.mandatory()) references.append("availability=\"mandatory\" ");
                    else references.append("availability=\"optional\" ");
                    if (rDef.timeout() > 0)
                        references.append("timeout=\"").append(rDef.timeout()).append("\" ");
                    if (rDef.ranking() > 0)
                        references.append("ranking=\"").append(rDef.ranking()).append("\" ");
                    references.append(">\n");
                    references
                            .append("  <reference-listener ref=\"bean\" bind-method=\"")
                            .append(rMethod)
                            .append("\" ");
                    Method unset = unsetMethods.get(rIfc);
                    if (unset != null)
                        references.append("unbind-method=\"").append(unset.getName()).append("\" ");
                    references.append(" />\n");
                    references.append("</reference>\n");
                }
            }
        }
        /*
        { // fields
            List<Field> list = MSystem.findFieldsWithAnnotation(clazz, ServiceReference.class);
            for (Field field : list) {
                ServiceReference rDef = field.getAnnotation(ServiceReference.class);
                String rName = "reff-" + field.getName();
                String rIfc = field.getType().getCanonicalName();
                String rField = field.getName();

                if (rIfc.equals(BlueprintContainer.class.getCanonicalName())) {
                    // special for bundle context
                    beanReferences.append("  <property name=\"").append(rField).append("\" ref=\"blueprintContainer\"/>\n");
                } else
                if (rIfc.equals(Bundle.class.getCanonicalName())) {
                    // special for bundle context
                    beanReferences.append("  <property name=\"").append(rField).append("\" ref=\"blueprintBundle\"/>\n");
                } else
                if (rIfc.equals(BundleContext.class.getCanonicalName())) {
                    // special for bundle context
                    beanReferences.append("  <property name=\"").append(rField).append("\" ref=\"blueprintBundleContext\"/>\n");
                } else {
                    // all others
                    if (rDef.service() != Object.class)
                        rIfc = rDef.service().getCanonicalName();
                    references.append("<reference id=\"").append(rName).append("\" interface=\"").append(rIfc).append("\" ");
                    if (rDef.mandatory())
                        references.append("availability=\"mandatory\" ");
                    else
                        references.append("availability=\"optional\" ");
                    if (rDef.timeout() > 0)
                        references.append("timeout=\"").append(rDef.timeout()).append("\" ");
                    if (rDef.ranking() > 0)
                        references.append("ranking=\"").append(rDef.ranking()).append("\" ");
                    references.append("/>\n");

                    beanReferences.append("  <property name=\"").append(rField).append("\" ref=\"").append(rName).append("\"/>\n");
                }
            }
        }
        */
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
        bean.append(beanReferences.toString());
        bean.append("</bean>\n");
        // create service

        StringBuilder service = new StringBuilder();

        Class<?>[] services = def.service();
        if (services.length == 0) {
            services = clazz.getInterfaces();
        }
        if (services.length == 0) throw new NotFoundException("interface for service not found");

        for (Class<?> srv : services) {
            service.append("<service interface=\"")
                    .append(srv.getCanonicalName())
                    .append("\" ref=\"bean\">\n");
            service.append("  <service-properties>\n");
            service.append("    <entry key=\"osgi.jndi.service.name\" value=\"")
                    .append(srv.getCanonicalName())
                    .append("\"/>\n");
            service.append("    <entry key=\"de.mhus.osgi.services.managed\" value=\"true\"/>\n");
            // service.append("    <entry key=\"").append(MOsgi.OBJECT_CLASS).append("\"
            // value=\"").append(srv.getCanonicalName()).append("\"/>\n");

            String name = def.name();
            if (MString.isEmpty(name)) name = srv.getCanonicalName();
            service.append("    <entry key=\"")
                    .append(MOsgi.COMPONENT_NAME)
                    .append("\" value=\"")
                    .append(MXml.encode(name))
                    .append("\"/>\n");

            String[] pid = def.configurationPid();
            if (pid.length == 0) pid = new String[] {srv.getCanonicalName()};
            service.append("    <entry key=\"")
                    .append(MOsgi.SERVICE_PID)
                    .append("\" value=\"")
                    .append(MXml.encode(MString.join(pid, ',')))
                    .append("\"/>\n");

            for (String prop : def.property()) {
                String k = MString.beforeIndex(prop, '=');
                String v = MString.afterIndex(prop, '=');
                service.append("    <entry key=\"")
                        .append(MXml.encode(k))
                        .append("\" value=\"")
                        .append(MXml.encode(v))
                        .append("\"/>\n");
            }
            service.append("  </service-properties>\n");
            service.append("</service>\n");
        }

        // print template

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("references", references.toString());
        properties.put("bean", bean.toString());
        properties.put("service", service.toString());

        String templateFile = "blueprint.xml";
        InputStream is = this.getClass().getResourceAsStream(templateFile);
        if (is == null) {
            throw new IllegalArgumentException(
                    "Template resource " + templateFile + " doesn't exist");
        }

        ByteArrayOutputStream sos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(sos);
        TemplateUtils.createFromTemplate(out, is, properties, true);

        return sos.toString();
    }

    private File getBlueprintFle(String implClass) {
        return MApi.getFile(
                SCOPE.DEPLOY, MFile.normalize(BLUEPRINT_PREFIX + implClass.toLowerCase()) + ".xml");
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
            if (f.isFile()
                    && f.getName().startsWith(BLUEPRINT_PREFIX)
                    && f.getName().endsWith(".xml")) {
                out.add(f.getName().substring(BLUEPRINT_PREFIX.length(), f.getName().length() - 4));
            }
        return out;
    }
}
