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

import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.core.keychain.MutableVaultSource;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "keychain-remove", description = "Remove key from source")
@Service
public class CmdKeychainRemove extends AbstractCmd {

    @Argument(
            index = 0,
            name = "id",
            required = true,
            description = "Ident of the key",
            multiValued = false)
    String id;

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

        if (sourcename == null) sourcename = MKeychain.SOURCE_DEFAULT;
        KeychainSource source = vault.getSource(sourcename);
        if (source == null) {
            System.out.println("*** Source not found!");
            return null;
        }
        MutableVaultSource mutable = source.getEditable();

        mutable.removeEntry(UUID.fromString(id));
        System.out.println("OK");

        return null;
    }
}
