package de.mhus.osgi.commands.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;

@Command(scope = "java", name = "finalization", description = "Trigger the finalization of the JVM")
public class CmdFinalization implements Action {

	@Override
	public Object execute(CommandSession session) throws Exception {
		System.runFinalization();
		return null;
	}

}
