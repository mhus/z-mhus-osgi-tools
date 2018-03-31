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
package de.mhus.karaf.crypt;

import java.security.Provider;
import java.security.Security;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "crypta", name = "info", description = "Security Informations")
@Service
public class CmdInfo extends MLog implements Action {

	@Argument(index=0, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

	@Override
	public Object execute() throws Exception {

			if (parameters != null && parameters.length > 0) {
				Provider provider = Security.getProvider(parameters[0]);
				System.out.println(">>> " + provider.getName() + "-" + provider.getVersion() + " " + provider.getInfo());
				ConsoleTable out = new ConsoleTable();
				out.setHeaderValues("Algorithm","Type");
				for (java.security.Provider.Service service : provider.getServices()) {
					if (parameters.length < 2 || parameters[1].equals(service.getType()))
						out.addRowValues(service.getAlgorithm(),service.getType());
//						System.out.println("  - " + service.getAlgorithm() + " " + service.getType());
				}
				out.print(System.out);
			} else {
				ConsoleTable out = new ConsoleTable();
				out.setHeaderValues("Name","Version","Info");
				for (Provider provider : Security.getProviders()) {
					out.addRowValues(provider.getName(), provider.getVersion(), provider.getInfo());
//					System.out.println(">>> " + provider.getName() + "-" + provider.getVersion() + " " + provider.getInfo());
//					for (java.security.Provider.Service service : provider.getServices()) {
//						System.out.println("  - " + service.getAlgorithm() + " " + service.getType());
//					}
				}
				out.print(System.out);
			}
			return null;
	}
}
