package de.mhus.osgi.commands.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "java", name = "gc", description = "Trigger the Garbage Collector of the JVM")
@Service
public class CmdGC implements Action {

	@Override
	public Object execute() throws Exception {
		System.gc();
		return null;
	}

}
