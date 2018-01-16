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
    
	@Override
	public Object execute() throws Exception {

		ServiceReference<?>[] res = FrameworkUtil.getBundle(CmdServiceInspect.class).getBundleContext().getAllServiceReferences(serviceName, null);
		
		if (res == null || res.length == 0) {
			System.out.println("Service not found");
			return null;
		}
				
		ServiceReference<?> ref = res[0];
		
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
