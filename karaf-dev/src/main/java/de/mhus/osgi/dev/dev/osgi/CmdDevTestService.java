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
package de.mhus.osgi.dev.dev.osgi;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "dev-testservice", description = "Dev Test Service Tool")
@Service
public class CmdDevTestService extends AbstractCmd {

    @Argument(index = 0, name = "cmd", required = true, description = "sayso", multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        if (cmd.equals("sayso")) {
            ITestService service = MOsgi.getService(ITestService.class);
            // TestService service = M.l(ITestService.class);
            String res = service.saySo();
            System.out.println("Answer: " + res);
        }

        return null;
    }
}
