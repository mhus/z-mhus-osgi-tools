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
package de.mhus.karaf.commands.shell;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

@Command(scope = "shell", name = "inspectClass", description = "Inspect a class")
@Service
public class CmdClassInspect extends AbstractCmd {

    @Argument(
            index = 0,
            name = "classname",
            required = true,
            description = "Class name",
            multiValued = false)
    String className;

    @SuppressWarnings("deprecation")
    @Override
    public Object execute2() throws Exception {

        OsgiBundleClassLoader cl = new OsgiBundleClassLoader();
        Class<?> clazz = cl.loadClass(className);

        System.out.println("Class " + className + ":");
        Bundle bundle = FrameworkUtil.getBundle(clazz);
        if (bundle == null) System.out.println("Unknown Bundle");
        else System.out.println("Bundle: " + bundle.getBundleId() + " " + bundle.getSymbolicName());
        System.out.println();

        System.out.println("Constructors: ");
        for (Constructor<?> con : clazz.getConstructors()) {
            boolean first = true;
            for (Parameter p : con.getParameters()) {
                if (first) System.out.print(" -> ");
                else System.out.print(",");
                System.out.println(p.getName() + " : " + p.getType().getCanonicalName());
            }
        }
        System.out.println("Fields:");
        for (Field field : clazz.getFields())
            System.out.println(
                    (field.isAccessible() ? " public " : " hidden ")
                            + field.getName()
                            + " : "
                            + field.getType().getCanonicalName());

        System.out.println("Methods:");
        for (Method method : clazz.getMethods()) {
            if (!method.getDeclaringClass().getName().equals("java.lang.Object")) {
                System.out.println(
                        " "
                                + method.getName()
                                + "          (declared in "
                                + method.getDeclaringClass().getName()
                                + ")");
                boolean first = true;
                for (Parameter p : method.getParameters()) {
                    if (first) System.out.print(" -> ");
                    else System.out.print(",");
                    System.out.print(p.getName() + " : " + p.getType().getCanonicalName());
                    first = false;
                }
                if (!first) System.out.println();
                if (method.getReturnType() != void.class) {
                    System.out.println(" <- " + method.getReturnType().getName());
                }
                System.out.println();
            }
        }

        return clazz;
    }
}
