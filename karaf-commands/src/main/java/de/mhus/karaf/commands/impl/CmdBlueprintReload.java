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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.IBlueprintManager;

@Command(scope = "service", name = "blue-reload", description = "Reload configured services")
@Service
public class CmdBlueprintReload extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        IBlueprintManager api = M.l(IBlueprintManager.class);
        api.reloadConfigured();
        System.out.println("OK");
        return null;
    }
}
