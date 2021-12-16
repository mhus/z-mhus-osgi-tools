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

import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.crypt.Blowfish;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.keychain.DefaultEntry;
import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.core.keychain.MutableVaultSource;
import de.mhus.lib.core.parser.ParseException;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "mhus",
        name = "keychain-add",
        description = "add a new entry into the keychain with the given contnt")
@Service
public class CmdKeychainAdd extends AbstractCmd {

    @Argument(
            index = 0,
            name = "content",
            required = true,
            description = "Content to add",
            multiValued = false)
    String content;

    @Argument(
            index = 1,
            name = "source",
            required = false,
            description = "Source",
            multiValued = false)
    String sourcename = null;

    @Option(
            name = "-p",
            aliases = {"--passphrase"},
            description = "Define a passphrase if required",
            required = false,
            multiValued = false)
    String passphraseO = null;

    @Option(
            name = "-id",
            description = "Optiona a existing uuid to import in raw mode",
            required = false)
    String idO = null;

    @Option(name = "-t", description = "Type to set", required = false)
    String typeO = null;

    @Option(name = "-n", description = "Name to set", required = false)
    String nameO = null;

    @Option(name = "-d", description = "Description to set", required = false)
    String descO = null;

    @Option(name = "-f", aliases = "--force", description = "Overwrite existing", required = false)
    boolean forceO = false;

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

        String type = typeO == null ? "" : typeO;
        String name = nameO == null ? "" : nameO;
        String description = descO == null ? "" : descO;

        if (passphraseO != null && content.contains("-----BEGIN CIPHER-----")) {
            if ("".equals(passphraseO)) {
                System.out.print("Passphrase: ");
                System.out.flush();
                passphraseO = Console.get().readPassword();
            }
            PemBlock pem = PemUtil.parse(content);
            if (idO == null && pem.containsKey(PemBlock.IDENT)) idO = pem.getString(PemBlock.IDENT);
            content = pem.getBlock();
            content = Blowfish.decrypt(content, passphraseO);
        }

        if (type.equals("")) type = MKeychainUtil.getType(content);
        PemBlock pem = null;
        try {
            pem = PemUtil.parse(content);
        } catch (ParseException e) {
        }
        if (pem != null && description.equals("")) {
            description = pem.getString(PemBlock.DESCRIPTION, "");
            if (idO == null) idO = pem.getString(PemBlock.IDENT, null);
        }
        //            if (pem != null && name.equals(""))
        //                name = pem.getString("");

        DefaultEntry entry = new DefaultEntry(type, name, description, content);
        if (idO != null)
            entry =
                    new DefaultEntry(
                            UUID.fromString(idO),
                            entry.getType(),
                            entry.getName(),
                            entry.getDescription(),
                            entry.getValue());

        if (mutable.getEntry(entry.getId()) != null) {
            if (forceO) mutable.removeEntry(entry.getId());
            else {
                System.out.println("*** Entry already exists " + entry.getId());
                return null;
            }
        }

        mutable.addEntry(entry);
        System.out.println("Created " + entry + ". Don't forget to save!");
        return entry.getId().toString();
    }
}
