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
package de.mhus.karaf.xdb.adb;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.services.adb.AdbUtilKaraf;
import de.mhus.osgi.services.adb.DbManagerService;

@Command(scope = "adb", name = "datasource", description = "Update ADB DataSource")
@Service
public class CmdDatasource implements Action {
	
	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;

	@Argument(index=1, name="source", required=false, description="Data Source", multiValued=false)
    String sourceName;
	
	@Override
	public Object execute() throws Exception {

		int cnt = 0;
		
		for ( DbManagerService service : AdbUtilKaraf.getAdmin().getServices()) {
//			if (service.isConnected()) {
				if (service.getClass().getCanonicalName().equals(serviceName)) {
					if (sourceName == null)
						service.updateManager(false);
					else
						service.setDataSourceName(sourceName);
					cnt++;
				}
//			}
		}
		System.out.println("Updated: " + cnt);
		return null;
	}

}
