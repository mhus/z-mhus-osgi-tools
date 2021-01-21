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
package de.mhus.karaf.commands.shell;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "service", name = "sls", description = "List of Services")
@Service
public class CmdServiceList extends AbstractCmd {

    @Argument(
            index = 0,
            name = "filter",
            required = false,
            description = "Filter Regular Expression over Services or service id",
            multiValued = false)
    String filter;

    @Option(
            name = "-i",
            aliases = {"--inspect"},
            description = "Get the service instance and insprect",
            required = false,
            multiValued = false)
    boolean inspect = false;

    @Override
    public Object execute2() throws Exception {

        ConsoleTable tbl = new ConsoleTable(tblOpt);
        tbl.setLineSpacer(true);
        if (inspect) tbl.setHeaderValues("Name", "Bundle", "Info");
        else tbl.setHeaderValues("Name", "Bundle", "Properties", "Using");

        int serviceIdFilter = MCast.toint(filter, -1);
        if (serviceIdFilter >= 0) filter = null;

        BundleContext ctx = FrameworkUtil.getBundle(CmdServiceList.class).getBundleContext();
        for (ServiceReference<?> ref : ctx.getAllServiceReferences(null, filter)) {
            if (serviceIdFilter >= 0
                    && MCast.toint(ref.getProperty(MOsgi.SERVICE_ID), -1) != serviceIdFilter)
                continue;
            Object name = ref.getProperty(MOsgi.COMPONENT_NAME);
            if (name == null) name = "[" + ref.getProperty(MOsgi.SERVICE_ID) + "]";
            else name = name.toString() + "\n[" + ref.getProperty(MOsgi.SERVICE_ID) + "]";
            StringBuilder props = new StringBuilder();
            for (String key : ref.getPropertyKeys())
                props.append(key)
                        .append('=')
                        .append(MString.toString(ref.getProperty(key)))
                        .append('\n');
            StringBuilder using = new StringBuilder();
            Bundle[] usingArray = ref.getUsingBundles();
            if (usingArray != null)
                for (Bundle u : usingArray)
                    using.append(u.getSymbolicName() + " [" + u.getBundleId() + "]");

            if (inspect) {
                String info = "";
                String clazz = "";
                try {
                    Object srv = ctx.getService(ref);
                    clazz = srv.getClass().getCanonicalName();
                    info = srv.toString();
                } catch (Throwable t) {
                    info = t.getMessage();
                }
                tbl.addRowValues(
                        name,
                        ref.getBundle().getSymbolicName()
                                + " ["
                                + ref.getBundle().getBundleId()
                                + "]\n("
                                + clazz
                                + ")",
                        info);
            } else
                tbl.addRowValues(
                        name,
                        ref.getBundle().getSymbolicName()
                                + " ["
                                + ref.getBundle().getBundleId()
                                + "]",
                        props,
                        using);
        }

        tbl.print();
        return null;
    }
}
