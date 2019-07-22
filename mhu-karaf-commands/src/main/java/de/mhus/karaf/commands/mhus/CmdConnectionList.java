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
package de.mhus.karaf.commands.mhus;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.osgi.api.jms.JmsDataSource;
import de.mhus.osgi.api.jms.JmsManagerService;
import de.mhus.osgi.api.jms.JmsUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "jms", name = "connection-list", description = "Remove connection")
@Service
public class CmdConnectionList extends AbstractCmd {

    @Option(name = "-ct", aliases = { "--console-table" }, description = "Console table options", required = false, multiValued = false)
    String consoleTable;

	@Override
	public Object execute2() throws Exception {
		JmsManagerService service = JmsUtil.getService();
		if (service == null) {
			System.out.println("Service not found");
			return null;
		}
		
		ConsoleTable table = new ConsoleTable(consoleTable);
		table.setHeaderValues("Id","Name","Url","User","Connected","Closed");
		for (de.mhus.osgi.api.services.MOsgi.Service<JmsDataSource> ref : service.getDataSources()) {
			try {
				JmsConnection con = ref.getService().getConnection();
				String name = service.getServiceName(ref);
				table.addRowValues(name,ref.getService().getName(),con.getUrl(),con.getUser(),con.isConnected(),con.isClosed());
			} catch (Throwable t) {}
		}
		table.print(System.out);
		
		return null;
	}

}
