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
package de.mhus.karaf.mongo;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.services.MOsgi;

@Command(scope = "mongo", name = "datasources", description = "List Mongo Datasources")
@Service
public class CmdMongoDataSources implements Action {

	@Option(name="-f", aliases="--full", description="Full output",required=false)
	boolean full = false;

	@Override
	public Object execute() throws Exception {
		
		ConsoleTable table = new ConsoleTable(full);
		
		table.setHeaderValues("Ref","Name","Connected","Host","Port");
		for (de.mhus.osgi.services.MOsgi.Service<MongoDataSource> ref : MOsgi.getServiceRefs(MongoDataSource.class, null)) {
			
			MongoDataSource service = ref.getService();
			table.addRowValues(ref.getReference().getProperty("lookup.name"),service.getName(),service.isConnected(),service.getHost(),service.getPort());
		}
		
		table.print(System.out);
		return null;
	}
	
}
