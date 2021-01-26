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

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.core.keychain.MutableVaultSource;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "keychain-sources", description = "List all sources")
@Service
public class CmdKeychainSources extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {
        MKeychain vault = MKeychainUtil.loadDefault();

        ConsoleTable out = new ConsoleTable(tblOpt);
        out.setHeaderValues("Source", "Info", "Mutable", "MemoryBased");
        for (String sourceName : vault.getSourceNames()) {
            KeychainSource source = vault.getSource(sourceName);

            boolean isMutable = false;
            boolean isMemoryBased = false;
            try {
                MutableVaultSource mutable = source.getEditable();
                isMutable = mutable != null;
                isMemoryBased = mutable.isMemoryBased();
                if (isMutable) {}
            } catch (Throwable t) {
            }
            out.addRowValues(sourceName, source, isMutable, isMemoryBased);
        }
        out.print(System.out);

        return null;
    }
}
