package de.mhus.osgi.jwskarafbridge.impl;

import java.io.PrintStream;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.jwsbridge.JavaWebServiceAdmin;
import de.mhus.osgi.jwsbridge.WebServiceInfo;

@Command(scope = "jws", name = "list", description = "List current Web Services")
public class CmdList implements Action {

	private JavaWebServiceAdmin admin;

	public Object execute(CommandSession session) throws Exception {
		PrintStream out = System.out;
		//session.getConsole();
		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Id","Name","Bundle","Status","Binding");
		
		for (WebServiceInfo info : admin.getWebServices()) {
			table.addRowValues(String.valueOf(info.getId()),info.getName(),info.getBundleName(),info.getStatus(),info.getBinding());
		}
		table.print(out);
		out.flush();
		return null;
	}
	
	public void setAdmin(JavaWebServiceAdmin admin) {
		this.admin = admin;
	}

}
