package de.mhus.osgi.commands.impl;

import java.io.PrintStream;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "bundle", name = "rawlist", description = "Return the raw list of bundle names")
public class CmdBundleList implements Action  {

    @Option(name = "-l", aliases = { "--location" }, description = "Print location", required = false, multiValued = false)
    boolean pLocation;

	@Argument(index=0, name="filter", required=false, description="Filter Regular Expression over Bundle Name", multiValued=false)
    String filter;

	private BundleContext context;

	public Object execute(CommandSession session) throws Exception {
		ConsoleTable table = new ConsoleTable();
		table.getHeader().add("id");
		table.getHeader().add("Bundle");
		table.getHeader().add("Version");
		table.getHeader().add("State");
		table.getHeader().add("Modified");
		if (pLocation)
			table.getHeader().add("Location");

		for (Bundle b : context.getBundles()) {
			
			if (filter == null || MString.compareRegexPattern(b.getSymbolicName(), filter)) {
			
				if (pLocation)
					table.addRowValues(""+b.getBundleId(),b.getSymbolicName(),b.getVersion().toString(), toState(b.getState()), MCast.toIsoDateTime(b.getLastModified()), b.getLocation() );
				else
					table.addRowValues(""+b.getBundleId(),b.getSymbolicName(),b.getVersion().toString(), toState(b.getState()), MCast.toIsoDateTime(b.getLastModified()) );
			}		
		}
		table.print(System.out);
		return null;
	}

	private String toState(int state) {
		switch (state) {
			case Bundle.ACTIVE:return "Active";
			case Bundle.INSTALLED: return "Installed";
			case Bundle.RESOLVED: return "Resolved";
			case Bundle.STARTING: return "Starting";
			case Bundle.STOPPING: return "Stopping";
			case Bundle.UNINSTALLED: return "Uninsitalled";
			default: return "" + state;
		}
	}

	public void setContext(BundleContext context) {
        this.context = context;
    }

}
