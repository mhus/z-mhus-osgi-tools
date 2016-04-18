package de.mhus.osgi.mail.karaf;

import java.io.PrintStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.mail.core.SendQueue;
import de.mhus.osgi.mail.core.SendQueueManager;

@Command(scope = "mail", name = "list", description = "List current Mail Send Queues")
@Service
public class CmdList implements Action {

	private SendQueueManager admin;

	@Override
	public Object execute() throws Exception {
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
