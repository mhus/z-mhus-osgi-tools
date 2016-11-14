package de.mhus.osgi.vaadinkarafbridge.impl;

import java.io.PrintStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceList", description = "List all resource providers")
@Service
public class CmdVaadinResourceList implements Action {

	@Reference
	private VaadinConfigurableResourceProviderAdmin provider;

	@Override
	public Object execute() throws Exception {
		PrintStream out = System.out;
		//session.getConsole();
		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Bundle","Resources");
		for (String s : provider.getResourceBundles()) {
			
			StringBuffer res = new StringBuffer();
			boolean first = true;
			for (String p : provider.getResourcePathes(s)) {
				if (!first) res.append(',');
				res.append(p);
				first = false;
			}
			table.addRowValues(s,res.toString());
		}
		table.print(out);
		out.flush();
		return null;
	}

}
