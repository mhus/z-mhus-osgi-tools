package de.mhus.osgi.jms;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

@Command(scope = "jms", name = "remove", description = "listen")
public class ReceiverRemoveCmd implements Action {

	@Argument(index=0, name="name", required=true, description="...", multiValued=false)
    String name;
	
	public Object execute(CommandSession s) throws Exception {
		
		JmsReceiverAdmin admin = JmsReceiverAdminImpl.findAdmin();
		admin.remove(name);
		
		return null;
		
	}
	
}
