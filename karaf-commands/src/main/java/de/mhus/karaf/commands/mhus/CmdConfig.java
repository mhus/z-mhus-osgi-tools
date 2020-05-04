/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.mhus;

import java.io.File;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.cfg.CfgProvider;
import de.mhus.lib.core.cfg.CfgValue;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.mapi.CfgManager;
import de.mhus.lib.core.mapi.IApi;
import de.mhus.lib.mutable.KarafMApiImpl;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "config", description = "Manipulate Configuration Values")
@Service
public class CmdConfig extends AbstractCmd {

    @Reference private Session session;

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command:\n"
                            + " list\n"
                            + " info\n"
                            + " set <owner> <path> <value>\n"
                            + " restart\n"
                            + " dump\n"
                            + " owners\n"
                            + " reload\n"
                            + " files",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    // private Appender appender;

    @Override
    public Object execute2() throws Exception {

        IApi s = MApi.get();
        if (!(s instanceof KarafMApiImpl)) {
            System.out.println("Karaf MApi not set");
            return null;
        }
        // KarafMApiImpl api = (KarafMApiImpl)s;

        switch (cmd) {
            case "info":
                {
                    CfgManager cfg = MApi.get().getCfgManager();
                    System.out.println(
                            "Last Update: " + MDate.toIso8601(cfg.getLastConfigUpdate()));
                }
                break;
            case "restart":
                {
                    MApi.get().getCfgManager().reConfigure();
                }
                break;
            case "list":
                {
                    ConsoleTable out = new ConsoleTable(tblOpt);
                    out.setHeaderValues(
                            "Owner", "Path", "Value", "Default", "Type", "Updated", "Calling");
                    for (CfgValue<?> value : MApi.getCfgUpdater().getList()) {
                        out.addRowValues(
                                value.getOwner(),
                                value.getPath(),
                                value.value(),
                                value.getDefault(),
                                MSystem.getSimpleName(value.getClass()),
                                MDate.toIso8601(value.getUpdated()),
                                value.getCalling());
                    }
                    out.print(System.out);
                }
                break;
            case "set":
                {
                    for (CfgValue<?> value : MApi.getCfgUpdater().getList()) {
                        if (value.getOwner().equals(parameters[0])
                                && value.getPath().equals(parameters[1])) {
                            value.setValue(parameters[2]);
                            System.out.println("OK");
                            break;
                        }
                    }
                }
                break;
            case "dump":
                {
                    for (CfgProvider provider : MApi.get().getCfgManager().getProviders()) {
                        System.out.println(">>> " + provider.getClass().getCanonicalName());
                        IConfig cfg = provider.getConfig();
                        System.out.println(cfg);
                        System.out.println("<<<");
                    }
                }
                break;
            case "owners":
                {
                    CfgManager api = MApi.get().getCfgManager();
                    for (String owner : api.getOwners()) {
                        System.out.println(">>> Owner: " + owner);
                        IConfig cfg = api.getCfg(owner);
                        System.out.println(cfg);
                    }
                }
                break;
            case "reload":
                {
                    CfgManager api = MApi.get().getCfgManager();
                    ((CfgManager.CentralMhusCfgProvider) api.getProviders().get(0))
                            .internalLoadConfig();
                    System.out.println("ok");
                }
                break;
            case "files":
                {
                    CfgManager api = MApi.get().getCfgManager();
                    for (File file :
                            ((CfgManager.CentralMhusCfgProvider) api.getProviders().get(0))
                                    .files()) {
                        System.out.println(file.getAbsolutePath());
                    }
                }
                break;
            default:
                System.out.println("Command not found");
        }

        return null;
    }
}
