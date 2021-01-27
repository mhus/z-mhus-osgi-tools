/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.ISimpleService;

@Command(scope = "mhus", name = "simpleservice-list", description = "Simple Service list")
@Service
public class CmdSimpleServiceList extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {

        ConsoleTable table = new ConsoleTable(tblOpt);
        table.setHeaderValues("Name", "Info", "Status");
        BundleContext context =
                FrameworkUtil.getBundle(CmdSimpleServiceList.class).getBundleContext();
        for (ServiceReference<ISimpleService> ref :
                context.getServiceReferences(ISimpleService.class, null)) {
            ISimpleService service = context.getService(ref);
            table.addRowValues(
                    service.getClass().getCanonicalName(),
                    service.getSimpleServiceInfo(),
                    service.getSimpleServiceStatus());
        }
        table.print(System.out);

        return null;
    }
}
