package de.mhus.osgi.jms;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "jms", name = "direct-list", description = "listen")
@Service
public class ReceiverListCmd implements Action {

	public Object execute() throws Exception {
		
		JmsReceiverAdmin admin = JmsReceiverAdminImpl.findAdmin();
		
		for (JmsReceiver r : admin.list())
			System.out.println(r.getName());
		
		return null;
		
	}
	
}
