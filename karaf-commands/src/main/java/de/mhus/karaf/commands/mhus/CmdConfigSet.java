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
import de.mhus.lib.core.cfg.CfgValue;
import de.mhus.lib.core.mapi.IApi;
import de.mhus.lib.mutable.KarafMApiImpl;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "config-set", description = "Set a config value")
@Service
public class CmdConfigSet extends AbstractCmd {

    @Argument(
            index = 0,
            name = "owner",
            required = true,
            description = "Owner",
            multiValued = false)
    String ownerA;

    @Argument(index = 1, name = "path", required = true, description = "Path", multiValued = false)
    String pathA;

    @Argument(
            index = 2,
            name = "value",
            required = true,
            description = "Value",
            multiValued = false)
    String valueA;

    @Override
    public Object execute2() throws Exception {

        IApi s = MApi.get();
        if (!(s instanceof KarafMApiImpl)) {
            System.out.println("Karaf MApi not set");
            return null;
        }

        for (CfgValue<?> value : MApi.getCfgUpdater().getList()) {
            if (value.getOwner().equals(ownerA) && value.getPath().equals(pathA)) {
                value.setValue(valueA);
                System.out.println("OK");
            }
        }

        return null;
    }
}
