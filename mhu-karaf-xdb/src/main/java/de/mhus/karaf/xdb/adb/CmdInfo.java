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

@Command(scope = "adb", name = "info", description = "Adb Service Info")
@Service
public class CmdInfo implements Action {
	
	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;

	@Override
	public Object execute() throws Exception {

		for ( DbManagerService service : AdbUtilKaraf.getAdmin().getServices()) {
			if (service.getClass().getCanonicalName().equals(serviceName)) {
				System.out.println("Pool     : " + service.getManager().getPool().getClass());
				System.out.println("Pool Size: " + service.getManager().getPool().getSize());
				System.out.println("Pool Used: " + service.getManager().getPool().getUsedSize());
				System.out.println("DataSource Name:" + service.getManager().getDataSourceName());
				System.out.println("Schema     : " + service.getManager().getSchema().getClass());
				System.out.println("Schema Name: " +service.getManager().getSchemaName());
			}
		}
		return null;
	}

}
