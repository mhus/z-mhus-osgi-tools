package de.mhus.osgi.commands.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "java", name = "finalization", description = "Trigger the finalization of the JVM")
@Service
public class CmdFinalization implements Action {

	@Override
	public Object execute() throws Exception {
		System.runFinalization();
		return null;
	}

}
