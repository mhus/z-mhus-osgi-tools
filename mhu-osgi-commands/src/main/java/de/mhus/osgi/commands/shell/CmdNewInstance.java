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
package de.mhus.osgi.commands.shell;

import java.lang.reflect.Constructor;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.karaf.util.OsgiBundleClassLoader;

@Command(scope = "shell", name = "newInstance", description = "Create a new instance of an object")
@Service
public class CmdNewInstance implements Action {

	@Argument(index=0, name="classname", required=true, description="Class name", multiValued=false)
    String className;

	@Argument(index=1, name="parameters", required=false, description="a", multiValued=true)
    Object[] parameters;
    
    @Option(name = "-t", aliases = { "--parameterTypes" }, description = "Parameter Types", required = false, multiValued = false)
    String pt;
    
	@Override
	public Object execute() throws Exception {
		
		OsgiBundleClassLoader cl = new OsgiBundleClassLoader();
		Class<?> clazz = cl.loadClass(className);
		
		Class<?>[] parameterTypes = null;
		
		if (pt != null) {
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
		
		Constructor<?> constr = clazz.getConstructor(parameterTypes);
		
		Object obj = constr.newInstance(parameters);
		System.out.println("OK");
		return obj;
	}

}
