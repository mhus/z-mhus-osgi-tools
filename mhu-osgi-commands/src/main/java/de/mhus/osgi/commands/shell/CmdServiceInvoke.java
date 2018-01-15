package de.mhus.osgi.commands.shell;

import java.lang.reflect.Method;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.karaf.MOsgi;

@Command(scope = "service", name = "invoke", description = "Invoke a service method")
@Service
public class CmdServiceInvoke implements Action {

	@Argument(index=0, name="service", required=true, description="Service name", multiValued=false)
    String serviceName;

	@Argument(index=1, name="method", required=true, description="Method name", multiValued=false)
    String methodName;

	@Argument(index=2, name="parameters", required=false, description="a", multiValued=true)
    Object[] parameters;

    @Option(name = "-f", aliases = { "--filter" }, description = "Osgi filter", required = false, multiValued = false)
    String filter;

    @Option(name = "-x", aliases = { "--index" }, description = "Index for more", required = false, multiValued = false)
    int index = -1;
    
    @Option(name = "-t", aliases = { "--parameterTypes" }, description = "Parameter Types", required = false, multiValued = false)
    String pt;
    
	@Override
	public Object execute() throws Exception {

		ServiceReference<?>[] res = FrameworkUtil.getBundle(CmdServiceInvoke.class).getBundleContext().getAllServiceReferences(serviceName, filter);
		
		if (res == null || res.length == 0) {
			System.out.println("Service not found");
			return null;
		}
		
		if (res.length > 1 && (index < 0 || index >= res.length)) {
			System.out.println("Index out of bounds. Define -x option:");
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
			String[] p = pt.split(",");
			parameterTypes = new Class[p.length];
			for (int i = 0; i < p.length; i++) {
				if (p[i].equals("?"))
					parameterTypes[i] = parameters[i].getClass();
				else
					parameterTypes[i] = Class.forName(p[i]);
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
		
		return methodRes;
	}

}
