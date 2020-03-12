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
package de.mhus.karaf.commands.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.console.ANSIConsole;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.errors.UsageException;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "highlight", description = "Highlight the found parts")
@Service
public class CmdHighlight extends AbstractCmd {

    @Argument(
            index = 0,
            name = "regex",
            required = true,
            description =
                    "Regex to search or starting with # sets the format for following regex foundings\n"
                            + "#<color> WHITE,BLACK,RED,GREEN,BLUE,YELLOW,MAGENTA,CYAN - set foreground\n"
                            + "#F<color> WHITE,BLACK,RED,GREEN,BLUE,YELLOW,MAGENTA,CYAN - set foreground\n"
                            + "#B<color> WHITE,BLACK,RED,GREEN,BLUE,YELLOW,MAGENTA,CYAN - set background\n"
                            + "#A<mark>      - start mark\n"
                            + "#O<mark>      - stop mark\n"
                            + "#I<boolean>   - switch case sensitive\n"
                            + "#R<reference> - specify the inserted reference\n"
                            + "#reset        - reset settings",
            multiValued = true)
    String[] regex;

    @Override
    public Object execute2() throws Exception {

        boolean color = Console.get().isAnsi();

        InputStreamReader isr = new InputStreamReader(System.in, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line = null;

        while ((line = br.readLine()) != null) {

            Console.COLOR fg = Console.COLOR.RED;
            Console.COLOR bg = null;
            String markStart = null;
            String markStop = null;
            boolean caseSensitive = false;
            int reference = 0;

            if (!color) {
                markStart = "*";
                markStop = "*";
            }

            for (String r : regex) {
                if (r.startsWith("#") && !r.startsWith("##")) {
                    r = r.substring(1);
                    try {
                        fg = Console.COLOR.valueOf(r.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        if (r.equals("reset")) {
                            fg = Console.COLOR.RED;
                            bg = null;
                            markStart = null;
                            markStop = null;
                            caseSensitive = false;
                            reference = 0;
                            if (!color) {
                                markStart = "*";
                                markStop = "*";
                            }
                        } else if (r.startsWith("F") || r.startsWith("f")) {
                            if (r.length() == 1) fg = null;
                            else fg = Console.COLOR.valueOf(r.substring(1).toUpperCase());
                        } else if (r.startsWith("B") || r.startsWith("b")) {
                            if (r.length() == 1) bg = null;
                            else bg = Console.COLOR.valueOf(r.substring(1).toUpperCase());
                        } else if (r.startsWith("A") || r.startsWith("a")) {
                            if (r.length() == 1) markStart = null;
                            else markStart = r.substring(1);
                        } else if (r.startsWith("O") || r.startsWith("o")) {
                            if (r.length() == 1) markStop = null;
                            else markStop = r.substring(1);
                        } else if (r.startsWith("I") || r.startsWith("i"))
                            caseSensitive = MCast.toboolean(r.substring(1), true);
                        else if (r.startsWith("R") || r.startsWith("r"))
                            reference = MCast.toint(r.substring(1), 0);
                        else {
                            throw new UsageException("Unknown format definition", line);
                        }
                    }
                } else {
                    if (r.startsWith("##")) r = r.substring(1); // remove first #

                    String replacement =
                            (color && fg != null ? ANSIConsole.ansiForeground(fg) : "")
                                    + (color && bg != null ? ANSIConsole.ansiBackground(bg) : "")
                                    + (markStart != null ? markStart : "")
                                    + "$"
                                    + reference
                                    + (markStop != null ? markStop : "")
                                    + (color && (fg != null || bg != null)
                                            ? ANSIConsole.ansiCleanup()
                                            : "");

                    String search = (caseSensitive ? "" : "(?i)") + r;
                    line = line.replaceAll(search, replacement);
                }
            }
            System.out.println(line);
        }

        return null;
    }
}
