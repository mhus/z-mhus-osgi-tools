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
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.cao.CaoDataSource;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.MOsgi;

@Command(scope = "cao", name = "ds-list", description = "List All CAO Datasources")
@Service
public class CmdCaoDs extends AbstractCmd {

	@Override
	public Object execute2() throws Exception {
		ConsoleTable out = new ConsoleTable(tableAll, tblOpt);
		out.setHeaderValues("Name","Type","Status");
		for (CaoDataSource ds : MOsgi.getServices(CaoDataSource.class, null)) {
			out.addRowValues(ds.getName(), ds.getType(), ds);
		}
		out.print(System.out);
		return null;
	}
	
}
