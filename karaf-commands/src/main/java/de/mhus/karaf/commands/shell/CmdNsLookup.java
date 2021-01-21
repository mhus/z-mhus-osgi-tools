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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "nslookup", description = "Lookup dns names and ip")
@Service
public class CmdNsLookup extends AbstractCmd {

    @Argument(
            index = 0,
            name = "name",
            required = true,
            description = "DNS name or IP",
            multiValued = false)
    String name;

    @Override
    public Object execute2() throws Exception {

        try {
            InetAddress ipAddress = InetAddress.getByName(name);
            System.out.println("Hostname : " + ipAddress.getHostName());
            System.out.println("Canonical: " + ipAddress.getCanonicalHostName());
            System.out.println("IP       : " + ipAddress.getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println("IP address not found for: " + name);
        }
        return null;
    }
}
