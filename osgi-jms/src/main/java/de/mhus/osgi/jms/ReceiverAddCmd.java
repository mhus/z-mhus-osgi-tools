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
package de.mhus.osgi.jms;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.jms.JmsConnection;
import de.mhus.osgi.api.jms.JmsReceiver;
import de.mhus.osgi.api.jms.JmsReceiverAdmin;
import de.mhus.osgi.api.jms.JmsUtil;

@Command(scope = "jms", name = "direct-listen", description = "listen")
@Service
public class ReceiverAddCmd implements Action {

    @Argument(
            index = 0,
            name = "url",
            required = true,
            description = "url or connection name",
            multiValued = false)
    String url;

    @Argument(index = 1, name = "queue", required = true, description = "...", multiValued = false)
    String queue;

    @Option(
            name = "-t",
            aliases = "--topic",
            description = "Use a topic instead of a queue",
            required = false)
    boolean topic = false;

    @Option(name = "-u", aliases = "--user", description = "User", required = false)
    String user = "admin";

    @Option(name = "-p", aliases = "--password", description = "Password", required = false)
    String password = "password";

    @Option(
            name = "-d",
            aliases = "--daemon",
            description = "Work in the background",
            required = false)
    boolean daemon = false;

    @Override
    public Object execute() throws Exception {

        JmsReceiverAdmin admin = JmsReceiverAdminImpl.findAdmin();
        JmsReceiver receiver = null;
        if (url.indexOf(':') > 0) {

            receiver = new JmsReceiverOpenWire(user, password, url, topic, queue);

            admin.add(receiver);

        } else {

            JmsConnection con = JmsUtil.getConnection(url);
            if (con == null) {
                System.out.println("Connection not found");
                return null;
            }

            receiver = new JmsReceiverOpenWire(con, topic, queue);

            admin.add(receiver);
        }

        System.out.println("Receiving messages");
        if (!daemon) {
            System.out.println("Press ctrl+c to stop receiving");
            try {
                while (true) {
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
            }
            receiver.close();
            System.out.println("Stopped receiving messages");
        }
        return null;
    }
}
