/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.IBlueprintManager;

@Command(scope = "service", name = "blue-create", description = "Create a service blueprint")
@Service
public class CmdBlueprintCreate extends AbstractCmd {

    @Argument(
            index = 0,
            name = "implementation",
            required = true,
            description = "Canonical name of implementation class",
            multiValued = false)
    String impl;

    @Argument(
            index = 1,
            name = "bundle",
            required = false,
            description = "Bundle name or id",
            multiValued = false)
    String bundleName;

    @Option(
            name = "-u",
            aliases = {"--update"},
            description = "Update if already exists",
            required = false,
            multiValued = false)
    boolean update = false;

    @Override
    public Object execute2() throws Exception {
        IBlueprintManager api = M.l(IBlueprintManager.class);
        boolean ret = update ? api.update(impl, bundleName) : api.create(impl, bundleName);

        if (ret) System.out.println("Created");
        else System.out.println("Skipped");
        return null;
    }
}
