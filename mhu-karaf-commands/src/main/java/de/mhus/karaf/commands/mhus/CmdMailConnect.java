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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.mail.MSendMail;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "mailconnect", description = "Send a mail via MSendMail")
@Service
public class CmdMailConnect extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {

        MSendMail sendMail = M.l(MSendMail.class);
        System.out.println("Server: " + sendMail.getMailTransport().getConnectInfo());
        System.out.println("From  : " + sendMail.getMailTransport().getFrom());
        try {
            sendMail.getMailTransport().getSession();
            System.out.println("Connected");
        } catch (Throwable t) {
            System.out.println("Error: " + t);
        }
        return null;
    }
}
