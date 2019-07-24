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
package de.mhus.karaf.xdb.cmd;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.xdb.XdbApi;
import de.mhus.osgi.api.xdb.XdbUtil;

@Command(scope = "xdb", name = "list", description = "List all DB Services")
@Service
public class CmdList extends AbstractCmd {

	@Option(name="-a", description="Api Name",required=false)
	String apiName;

    @Reference
    private Session session;

	@Override
	public Object execute2() throws Exception {
		
		apiName = XdbUtil.getApiName(session, apiName);

		XdbApi api = XdbUtil.getApi(apiName);

		ConsoleTable table = new ConsoleTable(tblOpt);
		table.setHeaderValues("Service","Schema","DataSource","Managed Types");
		for (String serviceName : api.getServiceNames()) {
			XdbService service = api.getService(serviceName);
			if (service == null) {
				System.out.println("*** Service is null: " + serviceName);
				continue;
			}
			if (service.isConnected()) {
				
				int c = 0;
				for (String typeName : service.getTypeNames() ) {
					if (c == 0) {
						table.addRowValues(
								serviceName,
								service.getSchemaName(),
								service.getDataSourceName(),
								typeName
							);
					} else {
						table.addRowValues(
								"",
								"",
								"",
								typeName
							);
						
					}
					c++;
				}
				if (c == 0) {
					table.addRowValues(
							serviceName,
							service.getSchemaName(),
							service.getDataSourceName(),
							""
						);
				}
			} else {
				table.addRowValues(
						serviceName,
						"[not connected]", 
						service.getDataSourceName(),
						""
					);
			}
		}
/*		
		DbManagerAdmin admin = AdbUtil.getAdmin();
		if (admin == null) {
			System.out.println("Admin not found");
			return null;
		}
		ConsoleTable table = new ConsoleTable(tblOpt);
		table.setHeaderValues("Nr","Service","Schema","DataSource","Managed Types");
		// iterate all services
		
		int cnt = 0;

		for ( DbManagerService service : AdbUtil.getServices(false)) {
			if (service.isConnected()) {
				DbManager manager = service.getManager();
				
				int c = 0;
				for (Class<? extends Persistable> type : manager.getSchema().getObjectTypes()) {
					if (c == 0) {
						table.addRowValues(
								"*" + cnt,
								service.getServiceName(),
								manager.getSchema().getClass().getSimpleName(),
								service.getDataSourceName(),
								type.getSimpleName()
							);
					} else {
						table.addRowValues(
								"",
								"",
								"",
								"",
								type.getSimpleName()
							);
						
					}
					c++;
				}
				if (c == 0) {
					table.addRowValues(
							"*" + cnt,
							service.getServiceName(),
							manager.getSchema().getClass().getSimpleName(),
							service.getDataSourceName(),
							""
						);
				}
			} else {
				table.addRowValues(
						"*" + cnt,
						service.getServiceName(),
						"[not connected]", 
						service.getDataSourceName(),
						""
					);
			}
			cnt++;
		}
		*/
		table.print(System.out);
		return null;
	}

}
