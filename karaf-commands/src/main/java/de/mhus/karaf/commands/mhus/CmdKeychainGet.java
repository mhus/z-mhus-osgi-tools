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

import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.keychain.KeyEntry;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "keychain-get", description = "Return a value")
@Service
public class CmdKeychainGet extends AbstractCmd {


    @Argument(
            index = 0,
            name = "id/name",
            required = true,
            description = "Ident of the key",
            multiValued = false)
    String name;

    @Argument(
            index = 1,
            name = "source",
            required = false,
            description = "Source",
            multiValued = false)
    String sourcename = null;

    @Override
    public Object execute2() throws Exception {
        MKeychain vault = MKeychainUtil.loadDefault();

        KeyEntry entry = null;
        if (sourcename != null) {
            if (MValidator.isUUID(name))
                entry = vault.getSource(sourcename).getEntry(UUID.fromString(name));
            else entry = vault.getSource(sourcename).getEntry(name);
        } else {
            if (MValidator.isUUID(name))
                entry = vault.getEntry(UUID.fromString(name));
            else entry = vault.getEntry(name);
        }
        if (entry == null) {
            System.out.println("*** Entry not found");
            return null;
        }
        System.out.println("Id         : " + entry.getId());
        System.out.println("Type       : " + entry.getType());
        System.out.println("Name       : " + entry.getName());
        System.out.println("Description: " + entry.getDescription());
        System.out.println(" Value");
        System.out.println("-------");
        System.out.println(entry.getValue().value());
        System.out.println("-------");
        return entry;

    }
}
