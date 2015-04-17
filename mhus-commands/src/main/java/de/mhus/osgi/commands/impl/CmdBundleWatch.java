package de.mhus.osgi.commands.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.osgi.commands.watch.PersistenceWatch;

@Command(scope = "bundle", name = "persistentwatch", description = "Work with persistence watch list")
public class CmdBundleWatch implements Action  {

	@Argument(index=0, name="cmd", required=true, description="Command add, remove,list, clear, watch, rewatch", multiValued=false)
    String cmd;

	@Argument(index=1, name="lines", required=false, description="lines to add or remove", multiValued=true)
    String[] lines;
	
	public Object execute(CommandSession session) throws Exception {
		
		PersistenceWatch service = MOsgi.getService(PersistenceWatch.class);

		if (cmd == null || cmd.equals("list")) {
			
			ConsoleTable table = new ConsoleTable();
			table.getHeader().add("Bundle");
			for (String line : service.list())
				table.addRowValues(line);
			
			table.print(System.out);
		} else
		if (cmd.equals("add")) {
			for (String line : lines)
				service.add(line);
			System.out.println("OK");
		} else
		if (cmd.equals("remove")) {
			for (String line : lines)
				service.remove(line);
			System.out.println("OK");
		} else
		if (cmd.equals("watch")) {
			service.watch();
		} else
		if (cmd.equals("clear")) {
			service.clear();
		} else
		if (cmd.equals("rewatch")) {
			service.watch();
		}
		return null;
	}

}
