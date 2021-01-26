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

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.keychain.KeychainSourceFromSecFile;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "keychain-tools", description = "Vault Manipulation Tools")
@Service
public class CmdKeychainTools extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Create keys with openssl: openssl genrsa -out private.pem 8192\n"
                            + "Commands:\n"
                            + " addfilesource <file> <passphrase>\n"
                            + " removesource <id>\n"
                            + " encodepasswordrot13 <clear>\n"
                            + " encodepasswordwithkey <key id> <clear>\n"
                            + " encodepasswordmd5 <clear>\n"
                            + " decodepassword <encoded password>\n"
                            + "",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Option(name = "-s", aliases = "--source", description = "Set vault source name")
    String sourcename = null;

    @Option(name = "-f", aliases = "--force", description = "Overwrite existing", required = false)
    boolean force = false;

    @Option(
            name = "-id",
            description = "Optiona a existing uuid to import in raw mode",
            required = false)
    String id = null;

    @Option(
            name = "-p",
            aliases = {"--passphrase"},
            description = "Define a passphrase if required",
            required = false,
            multiValued = false)
    String passphrase = null;

    @Override
    public Object execute2() throws Exception {
        MKeychain vault = MKeychainUtil.loadDefault();

        if (cmd.equals("addfilesource")) {
            KeychainSourceFromSecFile source =
                    new KeychainSourceFromSecFile(new File(parameters[0]), parameters[1]);
            vault.registerSource(source);
            System.out.println("Registered " + source);
        } else if (cmd.equals("removesource")) {
            vault.unregisterSource(parameters[0]);
            System.out.println("OK");
        } else if (cmd.equals("encodepasswordrot13")) {
            System.out.println(MPassword.encode(MPassword.METHOD.ROT13, parameters[0]));
        } else if (cmd.equals("encodepasswordwithkey")) {
            System.out.println(
                    MPassword.encode(MPassword.METHOD.RSA, parameters[1], parameters[0]));
        } else if (cmd.equals("encodepasswordmd5")) {
            System.out.println(MPassword.encode(MPassword.METHOD.HASH_MD5, parameters[1]));
        } else if (cmd.equals("decodepassword")) {
            System.out.println(MPassword.decode(parameters[0]));
        } else System.out.println("Command unknown");

        return null;
    }
}
