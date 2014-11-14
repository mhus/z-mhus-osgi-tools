package de.mhus.osgi.mail.karaf;

import java.io.PrintStream;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.mail.core.SendQueue;
import de.mhus.osgi.mail.core.SendQueueManager;

@Command(scope = "mail", name = "unregister", description = "Remove Mail Send Queues")
public class CmdUnregister implements Action {

	private SendQueueManager admin;

	@Argument(index=0, name="name", required=true, description="Queue Name", multiValued=false)
    String name;

	@Override
	public Object execute(CommandSession session) throws Exception {

		admin.unregisterQueue(name);
		System.out.println("OK");
		
		return null;
	}
	
	public void setAdmin(SendQueueManager admin) {
		this.admin = admin;
	}

}
