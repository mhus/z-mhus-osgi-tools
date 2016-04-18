package de.mhus.osgi.commands.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "java", name = "mem", description = "Print the current memory situation of the JVM")
@Service
public class CmdMem implements Action {

	@Override
	public Object execute() throws Exception {
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long max = Runtime.getRuntime().maxMemory();

		System.out.println("Free : " + free + " (" + (free / 1024 / 1024) + " MB)" );
		System.out.println("Total: " + total + " (" + (total / 1024 / 1024) + " MB)");
		System.out.println("Max  : " + max + " (" + (max / 1024 / 1024) + " MB)");
		return null;
	}

}
