package de.mhus.osgi.mail.karaf;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.mail.core.SendQueueManager;

@Command(scope = "mail", name = "unregister", description = "Remove Mail Send Queues")
@Service
public class CmdUnregister implements Action {

	private SendQueueManager admin;

	@Argument(index=0, name="name", required=true, description="Queue Name", multiValued=false)
    String name;

	@Override
	public Object execute() throws Exception {

		admin.unregisterQueue(name);
		System.out.println("OK");
		
		return null;
	}
	
	public void setAdmin(SendQueueManager admin) {
		this.admin = admin;
	}

}
