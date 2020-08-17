/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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
package de.mhus.karaf.commands.impl;

import java.io.IOException;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.PersistentWatch;

@Command(
        scope = "bundle",
        name = "persistentwatch",
        description = "Work with persistence watch list")
@Service
public class CmdBundleWatch extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = false,
            description = "Command add, remove,list, clear, watch, remember",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "lines",
            required = false,
            description = "lines to add or remove",
            multiValued = true)
    String[] lines;

    @Override
    public Object execute2() throws Exception {

        PersistentWatch service = MOsgi.getService(PersistentWatch.class);

        if (cmd == null || cmd.equals("list")) {
            print(service);
        } else if (cmd.equals("add")) {
            for (String line : lines) service.add(line);
            print(service);
            service.watch();
        } else if (cmd.equals("remove")) {
            for (String line : lines) service.remove(line);
            print(service);
            service.watch();
        } else if (cmd.equals("watch")) {
            service.watch();
        } else if (cmd.equals("clear")) {
            service.clear();
            print(service);
            service.watch();
        } else if (cmd.equals("remember")) {
            service.remember();
            print(service);
            service.watch();
        }
        return null;
    }

    private void print(PersistentWatch service) throws IOException {
        ConsoleTable table = new ConsoleTable(tblOpt);
        table.addHeader("Bundle");
        for (String line : service.list()) table.addRowValues(line);

        table.print(System.out);
    }
}
