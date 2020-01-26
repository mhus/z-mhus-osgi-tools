/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.impl;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.CacheControlIfc;
import de.mhus.osgi.api.services.CacheControlUtil;
import de.mhus.osgi.api.services.MOsgi;

@Command(scope = "mhus", name = "cache", description = "Cache Control Service Control")
@Service
public class CmdCacheControl extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description = "list,clear",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        if (cmd.equals("list")) {
            ConsoleTable table = new ConsoleTable(tblOpt);
            table.setHeaderValues("Name", "Size", "Enabled", "Status");
            for (CacheControlIfc c : MOsgi.getServices(CacheControlIfc.class, null))
                try {
                    table.addRowValues(c.getName(), c.getSize(), c.isEnabled(), "ok");
                } catch (Throwable t) {
                    log().d(c, t);
                    table.addRowValues(c.getName(), "?", "?", t.toString());
                }
            table.print(System.out);
        } else if (cmd.equals("clear")) {
            String name = null;
            if (parameters != null && parameters.length > 0) name = parameters[0];
            CacheControlUtil.clear(name);
            System.out.println("OK");

        } else if (cmd.equals("enable")) {
            String name = null;
            if (parameters != null && parameters.length > 0) name = parameters[0];
            CacheControlUtil.enable(name, true);
            System.out.println("OK");

        } else if (cmd.equals("disable")) {
            String name = null;
            if (parameters != null && parameters.length > 0) name = parameters[0];
            CacheControlUtil.enable(name, false);
            System.out.println("OK");
        }

        return null;
    }
}
