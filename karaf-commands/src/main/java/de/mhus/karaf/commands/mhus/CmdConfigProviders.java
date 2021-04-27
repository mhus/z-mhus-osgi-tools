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
package de.mhus.karaf.commands.mhus;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.mapi.IApi;
import de.mhus.lib.core.mapi.MCfgManager;
import de.mhus.lib.core.node.INode;
import de.mhus.lib.mutable.KarafMApiImpl;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "config-providers", description = "Show all config providers")
@Service
public class CmdConfigProviders extends AbstractCmd {

    @Argument(
            index = 0,
            name = "owner",
            required = false,
            description = "Owner filter",
            multiValued = false)
    String ownerFilter;

    @Override
    public Object execute2() throws Exception {

        IApi s = MApi.get();
        if (!(s instanceof KarafMApiImpl)) {
            System.out.println("Karaf MApi not set");
            return null;
        }

        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Owner", "Key", "Value", "Type");
        MCfgManager api = MApi.get().getCfgManager();
        for (String owner : api.getOwners()) {
            if (ownerFilter == null || ownerFilter.equals(owner)) {
                out.addRowValues(owner, "", "", "");
                INode cfg = api.getCfg(owner);
                for (String key : cfg.keys())
                    out.addRowValues(
                            "", key, cfg.get(key), cfg.get(key).getClass().getCanonicalName());
            }
        }
        out.print();

        return null;
    }
}
