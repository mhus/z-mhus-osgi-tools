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

import java.util.Arrays;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.ISimpleService;

@Command(scope = "mhus", name = "simpleservice-cmd", description = "Simple Service command")
@Service
public class CmdSimpleServiceCmd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "service",
            required = false,
            description = "Service Name regex",
            multiValued = false)
    String serviceName;

    @Argument(
            index = 1,
            name = "service cmd",
            required = true,
            description = "Cmd to the service",
            multiValued = false)
    String serviceCmd;

    @Argument(
            index = 2,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    Object[] parameters;

    @Override
    public Object execute2() throws Exception {

        BundleContext context =
                FrameworkUtil.getBundle(CmdSimpleServiceCmd.class).getBundleContext();
        for (ServiceReference<ISimpleService> ref :
                context.getServiceReferences(ISimpleService.class, null)) {
            ISimpleService service = context.getService(ref);
            if (service.getClass().getCanonicalName().matches(serviceName)) {
                System.out.println(
                        service.getClass().getCanonicalName()
                                + " CMD "
                                + serviceCmd
                                + " "
                                + Arrays.deepToString(parameters));
                service.doSimpleServiceCommand(serviceCmd, parameters);
            }
        }

        return null;
    }
}
