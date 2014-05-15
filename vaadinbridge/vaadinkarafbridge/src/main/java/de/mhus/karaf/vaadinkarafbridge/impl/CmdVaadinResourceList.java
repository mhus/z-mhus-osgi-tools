package de.mhus.karaf.vaadinkarafbridge.impl;

import java.io.PrintStream;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceList", description = "List a resource providers")
public class CmdVaadinResourceList implements Action {

	private VaadinConfigurableResourceProviderAdmin provider;

	public Object execute(CommandSession session) throws Exception {
		PrintStream out = session.getConsole();
		for (String s : provider.getResourceBundles()) {
			out.print(s);
			out.print(": ");
			boolean first = true;
			for (String p : provider.getResourcePathes(s)) {
				if (!first) out.print(',');
				out.print(p);
				first = false;
			}
			out.println();
		}
		out.flush();
		return null;
	}

	public void setResourceProvider(VaadinConfigurableResourceProviderAdmin provider) {
		this.provider = provider;
	}

}
