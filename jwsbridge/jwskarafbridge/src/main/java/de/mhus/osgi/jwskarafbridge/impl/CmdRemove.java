package de.mhus.osgi.jwskarafbridge.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.osgi.jwsbridge.JavaWebServiceAdmin;

@Command(scope = "jws", name = "remove", description = "Remove A Web Services")
public class CmdRemove implements Action {

	private JavaWebServiceAdmin admin;
	@Argument(index=0, name="service", required=true, description="Service Name", multiValued=false)
    String serviceName;

	public Object execute(CommandSession session) throws Exception {
		admin.closeWebService(serviceName);
		return null;
	}
	
	public void setAdmin(JavaWebServiceAdmin admin) {
		this.admin = admin;
	}

}
