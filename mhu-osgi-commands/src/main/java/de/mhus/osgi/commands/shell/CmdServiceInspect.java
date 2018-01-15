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

@Command(scope = "service", name = "inspect", description = "Inspect a service")
@Service
public class CmdServiceInspect implements Action {

	@Argument(index=0, name="service", required=true, description="Service name", multiValued=false)
    String serviceName;

    @Option(name = "-f", aliases = { "--filter" }, description = "Osgi filter", required = false, multiValued = false)
    String filter;

    @Option(name = "-x", aliases = { "--index" }, description = "Index for more", required = false, multiValued = false)
    int index = -1;
    
	@Override
	public Object execute() throws Exception {

		ServiceReference<?>[] res = FrameworkUtil.getBundle(CmdServiceInspect.class).getBundleContext().getAllServiceReferences(serviceName, filter);
		
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
			index = 0;
			System.out.println();
		}
		
		ServiceReference<?> ref = res.length == 1 ? res[0] : res[index];
		
		Object service = FrameworkUtil.getBundle(CmdServiceInspect.class).getBundleContext().getService(ref);
		
		Class<?> clazz = service.getClass();
		
		System.out.println("Service " + serviceName + ":");
		System.out.println();
		for (Method method : clazz.getMethods()) {
			if ( !method.getDeclaringClass().getName().equals("java.lang.Object")) {
				System.out.println(" " + method.getName() + "          (declared in " + method.getDeclaringClass().getName() + ")");
				for (Class<?> t : method.getParameterTypes())
					System.out.println(" -> " + t.getCanonicalName());
				if (method.getReturnType() != void.class)
					System.out.println(" <- " + method.getReturnType().getName());
				System.out.println();
			}
		}
		
		return null;
	}

}
