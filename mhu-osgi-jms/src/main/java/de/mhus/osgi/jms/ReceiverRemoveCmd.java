package de.mhus.osgi.jms;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "jms", name = "direct-remove", description = "listen")
@Service
public class ReceiverRemoveCmd implements Action {

	@Argument(index=0, name="name", required=true, description="...", multiValued=false)
    String name;
	
	public Object execute() throws Exception {
		
		JmsReceiverAdmin admin = JmsReceiverAdminImpl.findAdmin();
		admin.remove(name);
		
		return null;
		
	}
	
}
