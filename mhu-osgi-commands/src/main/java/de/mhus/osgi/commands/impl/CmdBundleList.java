package de.mhus.osgi.commands.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "bundle", name = "rawlist", description = "Return the raw list of bundle names")
@Service
public class CmdBundleList implements Action  {

    @Option(name = "-l", aliases = { "--location" }, description = "Print location", required = false, multiValued = false)
    boolean pLocation;

	@Argument(index=0, name="filter", required=false, description="Filter Regular Expression over Bundle Name", multiValued=false)
    String filter;

    @Option(name = "-m", aliases = { "--modified" }, description = "Order by modify date", required = false, multiValued = false)
    boolean orderModified;

    @Option(name = "-s", aliases = { "--symbolic" }, description = "Order by symbolic name", required = false, multiValued = false)
    boolean orderSymbolic;
    
    @Reference
	private BundleContext context;

	@Override
	public Object execute() throws Exception {
		ConsoleTable table = new ConsoleTable();
		table.getHeader().add("id");
		table.getHeader().add("Bundle");
		table.getHeader().add("Version");
		table.getHeader().add("State");
		table.getHeader().add("Modified");
		if (pLocation)
			table.getHeader().add("Location");
		table.getHeader().add("Valid");

		LinkedList<Object[]> list = new LinkedList<>();
		for (Bundle b : context.getBundles()) {
			
			String valid = "valid";
			try {
				b.getBundleContext().getBundle();
			} catch (Throwable e) {
				valid = e.getMessage();
			}
			
			if (filter == null || MString.compareRegexPattern(b.getSymbolicName(), filter)) {
				list.add(new Object[] { b, valid });
			}		
		}
		
		if (orderModified) {
			Collections.sort(list, new Comparator<Object[]>() {

				@Override
				public int compare(Object[] o1, Object[] o2) {
					int ret = Long.compare( ((Bundle)o1[0]).getLastModified(), ((Bundle)o2[0]).getLastModified() );
					if (ret == 0)
						ret = ((Bundle)o1[0]).getSymbolicName().compareTo( ((Bundle)o2[0]).getSymbolicName() );
					return ret;
				}
			} );
		} else
		if (orderSymbolic) {
			Collections.sort(list, new Comparator<Object[]>() {

				@Override
				public int compare(Object[] o1, Object[] o2) {
					int ret = ((Bundle)o1[0]).getSymbolicName().compareTo( ((Bundle)o2[0]).getSymbolicName() );
					return ret;
				}
			} );
		}
		
		for (Object[] l : list) {
			Bundle b = (Bundle) l[0];
			String valid = (String) l[1];
			
			if (pLocation)
				table.addRowValues(b.getBundleId(),b.getSymbolicName(),b.getVersion().toString(), toState(b.getState()), MDate.toIsoDateTime(b.getLastModified()), b.getLocation(), valid );
			else
				table.addRowValues(b.getBundleId(),b.getSymbolicName(),b.getVersion().toString(), toState(b.getState()), MDate.toIsoDateTime(b.getLastModified()), valid );
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
