package de.mhus.osgi.jms;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

@Command(scope = "jms", name = "direct-stomp-add", description = "listen")
public class ReceiverStompAddCmd implements Action {

	@Argument(index=0, name="url", required=true, description="...", multiValued=false)
    String url;
	
	@Argument(index=1, name="queue", required=true, description="...", multiValued=false)
    String queue;

	@Option(name="-t", aliases="--topic", description="Use a topic instead of a queue",required=false)
	boolean topic = false;
	
	@Option(name="-s", aliases="--synchronized", description="Create a temporary answer queue and wait for the answer",required=false)
	boolean sync = false;
	
	@Option(name="-u", aliases="--user", description="User",required=false)
	String user = "admin";
	
	@Option(name="-p", aliases="--password", description="Password",required=false)
	String password = "password";

	public Object execute(CommandSession s) throws Exception {
		
		JmsReceiver receiver = new JmsReceiverStomp(user, password, url, topic, queue);
		
		JmsReceiverAdmin admin = JmsReceiverAdminImpl.findAdmin();
		admin.add(receiver);
		
		return null;
		
	}
	
}
