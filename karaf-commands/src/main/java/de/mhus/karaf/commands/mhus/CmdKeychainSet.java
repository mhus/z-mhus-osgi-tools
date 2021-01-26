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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.keychain.DefaultEntry;
import de.mhus.lib.core.keychain.KeyEntry;
import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.core.keychain.MutableVaultSource;
import de.mhus.lib.core.util.SecureString;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "keychain-set", description = "Set proeprties of a key")
@Service
public class CmdKeychainSet extends AbstractCmd {

    @Argument(
            index = 0,
            name = "id",
            required = false,
            description = "Id of the key to change",
            multiValued = false)
    String id;

    @Argument(
            index = 1,
            name = "source",
            required = false,
            description = "Source",
            multiValued = false)
    String sourcename = null;

    @Option(
            name = "-t",
            description = "Type to set",
            required = false)
    String type = null;

    @Option(
            name = "-n",
            description = "Name to set",
            required = false)
    String name = null;
    
    @Option(
            name = "-d",
            description = "Description to set",
            required = false)
    String desc = null;
    
    @Option(
            name = "-v",
            description = "Value to set",
            required = false)
    String value = null;

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
        KeyEntry entry = mutable.getEntry(UUID.fromString(id));
        DefaultEntry newEntry =
                new DefaultEntry(
                        entry.getId(), 
                        type == null ? entry.getType() : type, 
                        name == null ? entry.getName() : name,
                        desc == null ? entry.getDescription() : desc,  
                        value == null ? entry.getValue() : new SecureString(value)
                    );
        mutable.updateEntry(newEntry);
        System.out.println("Set");

        return mutable;
    }
}
