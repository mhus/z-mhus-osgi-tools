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
package de.mhus.karaf.commands.shell;

import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.console.Console;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "read", description = "Read a line from stdin or a file")
@Service
public class CmdRead extends AbstractCmd {

    @Argument(
            index = 0,
            name = "fileName",
            required = false,
            description = "FileName to read from",
            multiValued = false)
    String fileName;

    @Option(
            name = "-o",
            aliases = {"--out"},
            description = "Store content into variable name",
            required = false,
            multiValued = false)
    String out;

    @Option(
            name = "-p",
            aliases = {"--prompt"},
            description = "Prompt",
            required = false,
            multiValued = false)
    String prompt;

    @Option(
            name = "-s",
            aliases = {"--secure"},
            description = "Echo stars instead of the characters",
            required = false,
            multiValued = false)
    boolean secure;

    @Reference private Session session;

    @Override
    public Object execute2() throws Exception {

        Console console = Console.get();

        @SuppressWarnings("unchecked")
        LinkedList<String> history = (LinkedList<String>) session.get("read_history");
        if (history == null) {
            history = new LinkedList<>();
            session.put("read_histrory", history);
        }

        String content = secure ? console.readPassword() : console.readLine(prompt, history);

        if (out != null) session.put(out, content);
        else return content;

        return null;
    }
}
