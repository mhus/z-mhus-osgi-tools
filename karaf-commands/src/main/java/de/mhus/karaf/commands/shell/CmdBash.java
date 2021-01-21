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

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MString;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "bash", description = "Execute bash line")
@Service
public class CmdBash extends AbstractCmd {

    @Argument(
            index = 0,
            name = "command",
            description = "Execution bash command with arguments",
            required = true,
            multiValued = true)
    private List<String> bashArgs;

    @Option(name = "-i", description = "Delegate Input", required = false, multiValued = true)
    boolean useInput = false;

    @Override
    public Object execute2() throws Exception {

        LinkedList<String> args = new LinkedList<>();
        args.add("bash");
        args.add("-c");
        args.add(MString.join(bashArgs.iterator(), " "));

        ProcessBuilder builder = new ProcessBuilder(args);

        PumpStreamHandler handler =
                new PumpStreamHandler(
                        useInput ? System.in : new ByteArrayInputStream(new byte[0]),
                        System.out,
                        System.err,
                        "Command" + args.toString());

        log().d("Executing", builder.command());
        Process p = builder.start();

        handler.attach(p);
        handler.start();

        log().d("Waiting for process to exit...");

        int status = p.waitFor();

        log().d("Process exited w/status", status);

        handler.stop();

        return null;
    }
}
