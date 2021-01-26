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

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.keychain.KeyEntry;
import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "keychain-list", description = "List all keys")
@Service
public class CmdKeychainList extends AbstractCmd {

    @Argument(
            index = 0,
            name = "source",
            required = false,
            description = "Source",
            multiValued = false)
    String sourcename;

    @Override
    public Object execute2() throws Exception {
        MKeychain vault = MKeychainUtil.loadDefault();


        if (sourcename == null) {
            ConsoleTable out = new ConsoleTable(tblOpt);
            out.setHeaderValues("Source", "Id", "Type", "Name", "Description");
            for (String sourceName : vault.getSourceNames()) {
                try {
                    KeychainSource source = vault.getSource(sourceName);
                    for (UUID id : source.getEntryIds()) {
                        KeyEntry entry = source.getEntry(id);
                        out.addRowValues(
                                sourceName,
                                id,
                                entry.getType(),
                                entry.getName(),
                                entry.getDescription());
                    }
                } catch (Throwable t) {
                    log().d(sourceName, t);
                }
            }
            out.print(System.out);
        } else {
            KeychainSource source = vault.getSource(sourcename);
            if (source == null) {
                System.out.println("*** Source not found!");
                return null;
            }
            ConsoleTable out = new ConsoleTable(tblOpt);
            out.setHeaderValues("Source", "Id", "Type", "Name", "Description");
            for (UUID id : source.getEntryIds()) {
                KeyEntry entry = source.getEntry(id);
                out.addRowValues(
                        sourcename,
                        id,
                        entry.getType(),
                        entry.getName(),
                        entry.getDescription());
            }
            out.print(System.out);
        }

        return null;
    }
}
