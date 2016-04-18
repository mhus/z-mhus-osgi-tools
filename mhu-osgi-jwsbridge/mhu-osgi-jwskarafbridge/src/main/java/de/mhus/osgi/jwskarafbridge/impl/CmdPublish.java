package de.mhus.osgi.jwskarafbridge.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.jwsbridge.JavaWebServiceAdmin;

@Command(scope = "jws", name = "publish", description = "Publish A Web Services")
@Service
public class CmdPublish implements Action {

	private JavaWebServiceAdmin admin;
	@Argument(index=0, name="service", required=true, description="Service Name", multiValued=false)
    String serviceName;

	public Object execute() throws Exception {
		admin.connect(serviceName);
		return null;
	}
	
	public void setAdmin(JavaWebServiceAdmin admin) {
		this.admin = admin;
	}

}
