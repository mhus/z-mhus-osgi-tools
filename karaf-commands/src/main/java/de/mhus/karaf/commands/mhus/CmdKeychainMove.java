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

import de.mhus.lib.core.keychain.KeyEntry;
import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.core.keychain.MutableVaultSource;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "keychain-move", description = "clone key and remove from old source")
@Service
public class CmdKeychainMove extends AbstractCmd {

    @Argument(
            index = 0,
            name = "id",
            required = true,
            description = "Id",
            multiValued = false)
    String id;

    @Argument(
            index = 1,
            name = "source",
            required = true,
            description = "Source",
            multiValued = false)
    String toSource = null;

    @Argument(
            index = 2,
            name = "source",
            required = false,
            description = "Source",
            multiValued = false)
    String sourcename = null;

    @Override
    public Object execute2() throws Exception {
        MKeychain vault = MKeychainUtil.loadDefault();

        if (sourcename == null) sourcename = MKeychain.SOURCE_DEFAULT;
        KeychainSource source = vault.getSource(sourcename);
        if (source == null) {
            System.out.println("*** Source not found " + sourcename);
            return null;
        }
        MutableVaultSource editable = source.getEditable();
        if (editable == null) {
            System.out.println("*** Source is not editable " + sourcename);
            return null;
        }
        KeyEntry entry = source.getEntry(UUID.fromString(id));
        if (entry == null) {
            System.out.println("*** Entry not found in source " + sourcename);
            return null;
        }
        vault.getSource(toSource).getEditable().addEntry(entry);
        editable.removeEntry(UUID.fromString(id));
        System.out.println("OK");

        return null;
    }
}
