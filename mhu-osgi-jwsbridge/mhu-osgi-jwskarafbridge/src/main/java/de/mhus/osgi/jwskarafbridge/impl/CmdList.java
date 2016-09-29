package de.mhus.osgi.jwskarafbridge.impl;

import java.io.PrintStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.jwsbridge.JavaWebServiceAdmin;
import de.mhus.osgi.jwsbridge.WebServiceInfo;

@Command(scope = "jws", name = "list", description = "List current Web Services")
@Service
public class CmdList implements Action {

	private JavaWebServiceAdmin admin;

	public Object execute() throws Exception {
		PrintStream out = System.out;
		//session.getConsole();
		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Id","Name","Bundle","Status","Binding");
		
		for (WebServiceInfo info : admin.getWebServices()) {
			try {
				table.addRowValues(String.valueOf(info.getId()),info.getName(),info.getBundleName(),info.getStatus(),info.getBindingInfo());
			} catch (Throwable t) {
				table.addRowValues(String.valueOf(info.getId()),info.getName(),"",t.toString(),"");
			}
		}
		table.print(out);
		out.flush();
		return null;
	}
	
	public void setAdmin(JavaWebServiceAdmin admin) {
		this.admin = admin;
	}

}
