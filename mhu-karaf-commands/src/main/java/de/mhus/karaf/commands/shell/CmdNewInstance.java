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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MSystem;
import de.mhus.osgi.services.util.OsgiBundleClassLoader;

@Command(scope = "shell", name = "newInstance", description = "Create a new instance of an object")
@Service
public class CmdNewInstance implements Action {

	@Argument(index=0, name="classname", required=true, description="Class name", multiValued=false)
    Object className;

	@Argument(index=1, name="parameters", required=false, description="a", multiValued=true)
    Object[] parameters;
    
    @Option(name = "-t", aliases = { "--parameterTypes" }, description = "Parameter Types", required = false, multiValued = false)
    String pt;
    
    @Option(name = "-p", aliases = { "--proxy" }, description = "Creates an proxy and call the shell command", required = false, multiValued = false)
    String proxyCmd;
    
    @Option(name = "-s", aliases = { "--set" }, description = "Set to parameter", required = false, multiValued = false)
    String set;
    
    @Reference
    private Session session;

	@Override
	public Object execute() throws Exception {
		
		OsgiBundleClassLoader cl = new OsgiBundleClassLoader();
		
		Class<?> clazz = null;
		if (className instanceof Class)
		    clazz = (Class<?>)className;
		else
		if (className instanceof String)
		    clazz = cl.loadClass(String.valueOf(className));
		else
		    clazz = className.getClass();
		
		Class<?>[] parameterTypes = null;
		
		if (proxyCmd != null) {
		    Object obj = Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz}, new MyInvocationHandler(session, clazz, proxyCmd));
		    if (set != null)
		        session.put(set, obj);
		    return obj;
		}
		
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
        if (set != null)
            session.put(set, obj);
		return obj;
	}

	private static class MyInvocationHandler implements InvocationHandler {

        private Session session;
        private String cmd;
        private Class<?> clazz;

        public MyInvocationHandler(Session session, Class<?> clazz, String proxyCmd) {
            this.session = session;
            this.clazz = clazz;
            this.cmd = proxyCmd;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString"))
                return "Proxy of " + clazz.getCanonicalName();
            session.put("args", args);
            return session.execute(cmd + " " + method.getName() + " $args");
        }
	    
	}
}
