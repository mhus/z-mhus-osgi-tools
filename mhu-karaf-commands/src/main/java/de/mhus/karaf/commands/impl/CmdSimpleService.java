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
package de.mhus.karaf.commands.impl;

import java.util.Arrays;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.services.SimpleServiceIfc;

@Command(scope = "mhus", name = "simpleservice", description = "Simple Service Interaction")
@Service
public class CmdSimpleService extends MLog implements Action {

	@Argument(index=0, name="cmd", required=true, description="list,cmd", multiValued=false)
    String cmd;

	@Argument(index=1, name="service", required=false, description="Service Name regex", multiValued=false)
    String serviceName;
	
	@Argument(index=2, name="service cmd", required=false, description="Cmd to the service", multiValued=false)
    String serviceCmd;

	@Argument(index=3, name="paramteters", required=false, description="Parameters", multiValued=true)
    Object[] parameters;

	@Override
	public Object execute() throws Exception {
		
		if (cmd.equals("list")) {
			ConsoleTable table = new ConsoleTable();
			table.setHeaderValues("Name","Info","Status");
			BundleContext context = FrameworkUtil.getBundle(CmdSimpleService.class).getBundleContext();
			for (ServiceReference<SimpleServiceIfc> ref : context.getServiceReferences(SimpleServiceIfc.class, null)) {
				SimpleServiceIfc service = context.getService(ref);
				table.addRowValues(service.getClass().getCanonicalName(), service.getSimpleServiceInfo(),service.getSimpleServiceStatus());
			}
			table.print(System.out);
		} else
		if (cmd.equals("cmd")) {
			BundleContext context = FrameworkUtil.getBundle(CmdSimpleService.class).getBundleContext();
			for (ServiceReference<SimpleServiceIfc> ref : context.getServiceReferences(SimpleServiceIfc.class, null)) {
				SimpleServiceIfc service = context.getService(ref);
				if (service.getClass().getCanonicalName().matches(serviceName) ) {
					System.out.println(service.getClass().getCanonicalName() + " CMD " + serviceCmd + " " + Arrays.deepToString(parameters));
					service.doSimpleServiceCommand(serviceCmd, parameters);
				}
			}
		}

		return null;
	}
	
}
