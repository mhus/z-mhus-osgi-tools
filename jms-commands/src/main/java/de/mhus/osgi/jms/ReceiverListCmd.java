package de.mhus.osgi.jms;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

@Command(scope = "jms", name = "list", description = "listen")
public class ReceiverListCmd implements Action {

	public Object execute(CommandSession s) throws Exception {
		
		JmsReceiverAdmin admin = JmsReceiverAdminImpl.findAdmin();
		
		for (JmsReceiver r : admin.list())
			System.out.println(r.getName());
		
		return null;
		
	}
	
}
