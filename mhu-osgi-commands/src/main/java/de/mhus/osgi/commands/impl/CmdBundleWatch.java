package de.mhus.osgi.commands.impl;

import java.io.IOException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.osgi.commands.watch.PersistentWatch;

@Command(scope = "bundle", name = "persistentwatch", description = "Work with persistence watch list")
@Service
public class CmdBundleWatch implements Action  {

	@Argument(index=0, name="cmd", required=false, description="Command add, remove,list, clear, watch, rewatch", multiValued=false)
    String cmd;

	@Argument(index=1, name="lines", required=false, description="lines to add or remove", multiValued=true)
    String[] lines;
	
	public Object execute() throws Exception {
		
		PersistentWatch service = MOsgi.getService(PersistentWatch.class);

		if (cmd == null || cmd.equals("list")) {
			print(service);
		} else
		if (cmd.equals("add")) {
			for (String line : lines)
				service.add(line);
			print(service);
			service.watch();
		} else
		if (cmd.equals("remove")) {
			for (String line : lines)
				service.remove(line);
			print(service);
			service.watch();
		} else
		if (cmd.equals("watch")) {
			service.watch();
		} else
		if (cmd.equals("clear")) {
			service.clear();
			print(service);
			service.watch();
		} else
		if (cmd.equals("remember")) {
			service.remember();
			print(service);
			service.watch();
		}
		return null;
	}
	
	private void print(PersistentWatch service) throws IOException {
		ConsoleTable table = new ConsoleTable();
		table.getHeader().add("Bundle");
		for (String line : service.list())
			table.addRowValues(line);
		
		table.print(System.out);
	}

}
