package de.mhus.osgi.commands.http;

import java.util.Properties;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.ops4j.pax.web.service.jetty.CentralRequestHandler;
import org.ops4j.pax.web.service.jetty.CentralRequestHandlerAdmin;
import org.ops4j.pax.web.service.jetty.ConfigurableHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "http", name = "centralenable", description = "Update central handlers list with properties")
public class CmdCentralHandlerEnable implements Action {

	@Argument(index=0, name="index", required=true, description="Nr of handler", multiValued=false)
    int index;

	@Argument(index=1, name="enabled", required=true, description="enabled", multiValued=false)
    boolean enabled;
	
	public Object execute(CommandSession session) throws Exception {
		
		BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();

		ServiceReference<CentralRequestHandlerAdmin> ref = ctx.getServiceReference(CentralRequestHandlerAdmin.class);
		if (ref == null) {
			System.out.println("CentralRequestHandlerAdmin not found");
			return null;
		}
		CentralRequestHandlerAdmin admin = ctx.getService(ref);
		if (admin == null) {
			System.out.println("CentralRequestHandlerAdmin not found");
			return null;
		}
		
		CentralRequestHandler handler = admin.getCentralHandlers()[index];
		if (handler instanceof ConfigurableHandler) {
			((ConfigurableHandler)handler).setEnabled(enabled);
			System.out.println("OK");
		} else
			System.out.println("Handler is not configurable");
		return null;
	}
}
