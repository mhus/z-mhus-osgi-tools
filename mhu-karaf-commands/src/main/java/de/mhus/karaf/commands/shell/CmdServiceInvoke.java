/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MSystem;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

@Command(scope = "service", name = "invoke", description = "Invoke a service method")
@Service
public class CmdServiceInvoke extends AbstractCmd {

	@Argument(index=0, name="service", required=true, description="Service name", multiValued=false)
    String serviceName;

	@Argument(index=1, name="method", required=true, description="Method name", multiValued=false)
    String methodName;

	@Argument(index=2, name="parameters", required=false, description="a", multiValued=true)
    Object[] parameters;

    @Option(name = "-f", aliases = { "--filter" }, description = "Osgi filter", required = false, multiValued = false)
    String filter;

    @Option(name = "-x", aliases = { "--index" }, description = "Index of the invoked service", required = false, multiValued = false)
    int index = -1;
    
    @Option(name = "-t", aliases = { "--parameterTypes" }, description = "Parameter Types", required = false, multiValued = false)
    String pt;
    
	@Override
	public Object execute2() throws Exception {

		ServiceReference<?>[] res = FrameworkUtil.getBundle(CmdServiceInvoke.class).getBundleContext().getAllServiceReferences(serviceName, filter);
		
		if (res == null || res.length == 0) {
			System.out.println("Service not found");
			return null;
		}
		
		if (res.length > 1 && (index < 0 || index >= res.length)) {
			System.out.println("More then one services found. \nUse -x option to select one of them or -f to filter the results:");
			int cnt = 0;
			for (ServiceReference<?> r : res) {
				System.out.println( " " + cnt + ": ");
				for (String n : r.getPropertyKeys())
					System.out.println("   " + n + "=" + r.getProperty(n));
				cnt++;
			}
			return null;
		}
		
		ServiceReference<?> ref = res.length == 1 ? res[0] : res[index];
		
		Object service = FrameworkUtil.getBundle(CmdServiceInvoke.class).getBundleContext().getService(ref);
		
		Class<?> clazz = service.getClass();
		
		Class<?>[] parameterTypes = null;
		
		if (pt != null) {
			OsgiBundleClassLoader cl = new OsgiBundleClassLoader();
			String[] p = pt.split(",");
			parameterTypes = new Class[p.length];
			for (int i = 0; i < p.length; i++) {
				if (p[i].equals("?"))
					parameterTypes[i] = parameters[i].getClass();
				else {
					parameterTypes[i] = MSystem.loadClass(p[i], cl);
					parameters[i] = MCast.toType(parameters[i], parameterTypes[i], MCast.getDefaultPrimitive(parameterTypes[i]));
				}
			}
		} else
		if (parameters != null) {
			parameterTypes = new Class[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				parameterTypes[i] = parameters[i].getClass();
			}
		} else {
			parameterTypes = new Class[0];
		}
		
		Method method = clazz.getMethod(methodName, parameterTypes);

		Object methodRes = method.invoke(service, parameters);
		System.out.println("OK");
		return methodRes;
	}

}
