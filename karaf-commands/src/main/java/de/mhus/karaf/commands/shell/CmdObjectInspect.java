/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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

import java.lang.reflect.Method;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.core.MDate;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "inspectObject", description = "Inspect a object")
@Service
public class CmdObjectInspect extends AbstractCmd {

    @Argument(
            index = 0,
            name = "classname",
            required = true,
            description = "Class name",
            multiValued = false)
    Object object;

    @Override
    public Object execute2() throws Exception {

        Class<? extends Object> clazz = object.getClass();

        Bundle bundle = FrameworkUtil.getBundle(clazz);
        if (bundle == null) System.out.println("Bundle is null");
        else {
            System.out.println(
                    "Bundle: " + bundle.getSymbolicName() + " [" + bundle.getBundleId() + "]");
            System.out.println("  Modified: " + MDate.toIso8601(bundle.getLastModified()));
            System.out.println("  Status  : " + MOsgi.getState(bundle));
        }

        System.out.println("Class " + clazz.getCanonicalName() + ":");
        System.out.println();
        for (Method method : clazz.getMethods()) {
            if (!method.getDeclaringClass().getName().equals("java.lang.Object")) {
                System.out.println(
                        " "
                                + method.getName()
                                + "          (declared in "
                                + method.getDeclaringClass().getName()
                                + ")");
                boolean first = true;
                for (Class<?> t : method.getParameterTypes()) {
                    if (first) System.out.print(" -> ");
                    else System.out.print(",");
                    System.out.print(t.getCanonicalName());
                    first = false;
                }
                if (!first) System.out.println();
                if (method.getReturnType() != void.class) {
                    System.out.println(" <- " + method.getReturnType().getName());
                }
                System.out.println();
            }
        }

        return null;
    }
}
