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

import java.io.File;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.crypt.Blowfish;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.keychain.DefaultEntry;
import de.mhus.lib.core.keychain.KeyEntry;
import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.core.keychain.MutableVaultSource;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "mhus",
        name = "keychain-import",
        description = "Add a new entry to the keychanin from a given file")
@Service
public class CmdKeychainImport extends AbstractCmd {

    private static final long MAX_FILE_LENGTH = 1024 * 1024; // max 1 MB

    @Argument(
            index = 0,
            name = "file",
            required = true,
            description = "File name and path",
            multiValued = false)
    String fileName;

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

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("*** File not found");
            return null;
        }
        if (!file.isFile()) {
            System.out.println("*** File is not an file");
            return null;
        }
        if (file.length() > MAX_FILE_LENGTH) {
            System.out.println("*** File to large to load");
            return null;
        }

        String name = nameO != null ? nameO + ";" : "";
        String desc = (descO != null ? descO + ";" : "") + file.getName();
        String content = MFile.readFile(file);
        if (passphraseO != null && content.contains("-----BEGIN CIPHER-----")) {
            if ("".equals(passphraseO)) {
                System.out.print("Passphrase: ");
                System.out.flush();
                passphraseO = Console.get().readPassword();
            }
            PemBlock pem = PemUtil.parse(content);
            if (idO == null && pem.containsKey(PemBlock.IDENT)) idO = pem.getString(PemBlock.IDENT);
            desc = pem.getString(PemBlock.DESCRIPTION, desc);
            content = pem.getBlock();
            content = Blowfish.decrypt(content, passphraseO);
        }

        String type = typeO != null ? typeO : MKeychainUtil.getType(content);
        KeyEntry entry = new DefaultEntry(type, name, desc, content);

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
        System.out.println(
                "Created " + entry.getId() + " " + entry.getType() + ". Don't forget to save!");
        return entry.getId().toString();
    }
}
