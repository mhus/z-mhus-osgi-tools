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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.cfg.CfgValue;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.mapi.IApi;
import de.mhus.lib.mutable.KarafMApiImpl;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "config-listeners", description = "Show all config listeners")
@Service
public class CmdConfigListeners extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {

        IApi s = MApi.get();
        if (!(s instanceof KarafMApiImpl)) {
            System.out.println("Karaf MApi not set");
            return null;
        }
        
        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues(
                "Owner", "Path", "Value", "Default", "Type", "Updated", "Calling");
        for (CfgValue<?> value : MApi.getCfgUpdater().getList()) {
            out.addRowValues(
                    value.getOwner(),
                    value.getPath(),
                    value.value(),
                    value.getDefault(),
                    MSystem.getSimpleName(value.getClass()),
                    MDate.toIso8601(value.getUpdated()),
                    value.getCalling());
        }
        out.print(System.out);

        return null;
    }
}
