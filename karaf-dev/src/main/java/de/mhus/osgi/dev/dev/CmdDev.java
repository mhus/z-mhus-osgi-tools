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
package de.mhus.osgi.dev.dev;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;

import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "dev", description = "Dev tools")
@Service
public class CmdDev extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description = "updateall\n" + "stopstartall\n",
            multiValued = false)
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

        if (cmd.equals("updateall")) {
            for (Bundle bundle : MOsgi.getBundleContext().getBundles()) {
                if (bundle.getVersion().toString().endsWith(".SNAPSHOT")) {
                    System.out.println(
                            ">>> " + bundle.getSymbolicName() + ":" + bundle.getVersion());
                    try {
                        bundle.update();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        } else if (cmd.equals("stopstartall")) {
            for (Bundle bundle : MOsgi.getBundleContext().getBundles()) {
                if (bundle.getVersion().toString().endsWith(".SNAPSHOT")) {
                    System.out.println(
                            ">>> " + bundle.getSymbolicName() + ":" + bundle.getVersion());
                    try {
                        bundle.stop();
                        bundle.start();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }

        return null;
    }
}
