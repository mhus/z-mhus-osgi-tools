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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "lls", description = "Local List Directory")
@Service
public class CmdLocalLs extends AbstractCmd {

    @Argument(
            index = 0,
            name = "files",
            description = "Files to show",
            required = false,
            multiValued = true)
    private List<String> files;

    @Option(name = "-l", description = "List in long format", required = false, multiValued = false)
    boolean lonfFormat = false;

    @Override
    public Object execute2() throws Exception {

        if (files == null) {
            files = new LinkedList<>();
            files.add(".");
        }

        for (String file : files) {
            File f = new File(file);
            if (f.isFile()) {
                print(f);
            } else {
                System.out.println(f.getName() + ":");
                for (File ff : f.listFiles()) {
                    print(ff);
                }
            }
        }

        return null;
    }

    private void print(File f) {
        if (lonfFormat) {
            System.out.println(
                    MFile.getUnixPermissions(f)
                            + " "
                            + MString.leftPad(String.valueOf(f.length()), 10, false)
                            + " "
                            + f.getName());
        } else {
            System.out.println(f.getName());
        }
    }
}
