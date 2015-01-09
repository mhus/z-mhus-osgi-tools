package de.mhus.osgi.mail.karaf;

import java.io.PrintStream;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.mail.core.SendQueue;
import de.mhus.osgi.mail.core.SendQueueManager;

@Command(scope = "mail", name = "list", description = "List current Mail Send Queues")
public class CmdList implements Action {

	private SendQueueManager admin;

	@Override
	public Object execute(CommandSession session) throws Exception {
		PrintStream out = System.out;
		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("id","valid","status");
		
		for (String name : admin.getQueueNames()) {
			try {
				SendQueue queue = admin.getQueue(name);
				table.addRowValues(name, ""+queue.isValid(), queue.getStatus());
			} catch (Throwable t) {
				table.addRowValues(name, "error", t.toString());
			}
		}
		table.print(out);
		out.flush();
		return null;
	}
	
	public void setAdmin(SendQueueManager admin) {
		this.admin = admin;
	}

}
