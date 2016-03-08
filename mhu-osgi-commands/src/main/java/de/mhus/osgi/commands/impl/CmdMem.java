package de.mhus.osgi.commands.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;

@Command(scope = "java", name = "mem", description = "Print the current memory situation of the JVM")
public class CmdMem implements Action {

	@Override
	public Object execute(CommandSession session) throws Exception {
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long max = Runtime.getRuntime().maxMemory();

		System.out.println("Free : " + free + " (" + (free / 1024 / 1024) + " MB)" );
		System.out.println("Total: " + total + " (" + (total / 1024 / 1024) + " MB)");
		System.out.println("Max  : " + max + " (" + (max / 1024 / 1024) + " MB)");
		return null;
	}

}
