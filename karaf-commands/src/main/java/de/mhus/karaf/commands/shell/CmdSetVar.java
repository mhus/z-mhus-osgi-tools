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
package de.mhus.karaf.commands.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "shell",
        name = "setvar",
        description = "Set a variable with a value, used in pipes")
@Service
public class CmdSetVar extends AbstractCmd {

    @Argument(
            index = 0,
            name = "names",
            description = "Name of the variable to set",
            required = true,
            multiValued = true)
    private String[] names;

    @Option(
            name = "-f",
            aliases = {"--forward"},
            description = "Output the value to the pipe",
            required = false,
            multiValued = false)
    boolean forward = false;

    @Reference private Session session;

    @Override
    public Object execute2() throws Exception {

        InputStreamReader isr = new InputStreamReader(System.in, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        int cnt = 0;
        while ((line = br.readLine()) != null) {
            if (cnt < names.length) session.put(names[cnt], line);
            cnt++;
            if (forward) System.out.println(line);
        }
        return null;
    }
}
