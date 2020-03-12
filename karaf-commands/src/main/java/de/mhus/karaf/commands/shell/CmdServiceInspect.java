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
package de.mhus.karaf.commands.shell;

import java.lang.reflect.Method;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "service", name = "inspect", description = "Inspect a service")
@Service
public class CmdServiceInspect extends AbstractCmd {

    @Argument(
            index = 0,
            name = "service",
            required = true,
            description = "Service name",
            multiValued = false)
    String serviceName;

    @Option(
            name = "-x",
            aliases = {"--index"},
            description = "Index of the inspected service",
            required = false,
            multiValued = false)
    int index = 0;

    @Override
    public Object execute2() throws Exception {

        ServiceReference<?>[] res =
                FrameworkUtil.getBundle(CmdServiceInspect.class)
                        .getBundleContext()
                        .getAllServiceReferences(serviceName, null);

        if (res == null || res.length == 0) {
            System.out.println("Service not found");
            return null;
        }

        ServiceReference<?> ref = res[index];

        Object service =
                FrameworkUtil.getBundle(CmdServiceInspect.class).getBundleContext().getService(ref);

        Class<?> clazz = service.getClass();

        System.out.println("Service " + serviceName + ":");
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
