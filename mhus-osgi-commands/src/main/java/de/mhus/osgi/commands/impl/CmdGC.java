package de.mhus.osgi.commands.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;

@Command(scope = "java", name = "gc", description = "Trigger the Garbage Collector of the JVM")
public class CmdGC implements Action {

	@Override
	public Object execute(CommandSession session) throws Exception {
		System.gc();
		return null;
	}

}
