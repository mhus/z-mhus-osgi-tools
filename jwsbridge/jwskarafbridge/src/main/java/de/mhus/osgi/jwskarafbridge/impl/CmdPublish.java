package de.mhus.osgi.jwskarafbridge.impl;

import java.io.PrintStream;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.jwsbridge.JavaWebServiceAdmin;
import de.mhus.osgi.jwsbridge.WebServiceInfo;

@Command(scope = "jws", name = "publish", description = "Publish A Web Services")
public class CmdPublish implements Action {

	private JavaWebServiceAdmin admin;
	@Argument(index=0, name="service", required=true, description="Service Name", multiValued=false)
    String serviceName;

	public Object execute(CommandSession session) throws Exception {
		admin.connect(serviceName);
		return null;
	}
	
	public void setAdmin(JavaWebServiceAdmin admin) {
		this.admin = admin;
	}

}
