package de.mhus.karaf.xdb.adb;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "adb", name = "pool", description = "Handle SQL DB Pools")
@Service
public class CmdDbPool implements Action {

	@Argument(index=0, name="command", required=true, description="Command: list, clear <id>", multiValued=false)
    String cmd;

	@Argument(index=1, name="arguments", required=false, description="Arguments for the command", multiValued=true)
    String[] args;
	
	@Override
	public Object execute() throws Exception {
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		
		if (cmd.equals("list")) {
			ConsoleTable out = new ConsoleTable();
			out.setHeaderValues("Id", "Name","Class", "Size", "Used");
			for (ObjectInstance instance : server.queryMBeans(null, null)) {
				if (instance.getObjectName().getCanonicalName().startsWith("de.mhus.lib.core.jmx.JmxObject:name=de.mhus.lib.sql.DbPool")) {
					Object size = null;
					Object usedSize = null;
					Object id = null;
					try {
						id = server.getAttribute(instance.getObjectName(), "PoolId");
						size = server.getAttribute(instance.getObjectName(), "Size");
						usedSize = server.getAttribute(instance.getObjectName(), "UsedSize");
					} catch (Throwable t) {}
					if (size != null)
						out.addRowValues(id, instance.getObjectName().getCanonicalName(), instance.getClassName(), size, usedSize );
				}
			}
			out.print(System.out);
		} else
		if (cmd.equals("clear")) {
			
			for (ObjectInstance instance : server.queryMBeans(null, null)) {
				if (instance.getObjectName().getCanonicalName().startsWith("de.mhus.lib.core.jmx.JmxObject:name=de.mhus.lib.sql.DbPool")) {
					try {
						Object id = server.getAttribute(instance.getObjectName(), "PoolId");
						if (args[0].equals(id)) {
							System.out.println("Clear: " + instance.getObjectName());
							server.invoke(instance.getObjectName(), "cleanup", new Object[] {true}, new String[] {boolean.class.getName()});
						}
					} catch (Throwable t) {t.printStackTrace();}
				}
			}
		} else
		if (cmd.equals("jmx-all")) {
			for (ObjectInstance instance : server.queryMBeans(null, null)) {
				System.out.println("MBean Found");
	            System.out.println("Class Name : " + instance.getClassName());
	            System.out.println("Object Name: " + instance.getObjectName());
	            System.out.println("****************************************");
			}
		}
		
		return null;
	}
	 
}
