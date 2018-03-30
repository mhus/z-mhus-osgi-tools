/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
