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

import java.net.URL;

import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/*
http://activemq.apache.org/stomp.html

*/
@Command(scope = "jms", name = "direct-stomp-send", description = "send")
@Service
public class StompSendCmd implements Action {

    @Argument(index = 0, name = "url", required = true, description = "...", multiValued = false)
    String url;

    @Argument(index = 1, name = "queue", required = true, description = "...", multiValued = false)
    String queue;

    @Argument(index = 2, name = "msg", required = true, description = "...", multiValued = false)
    String msg;

    @Argument(index = 3, name = "count", required = false, description = "...", multiValued = false)
    int count = 1;

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

    @Override
    public Object execute() throws Exception {

        StompConnection connection = new StompConnection();
        try {
            URL u = new URL(url);
            connection.open(u.getHost(), u.getPort());
            connection.connect(user, password);
            /*
            StompFrame connect = connection.receive();
            if (!connect.getAction().equals(Stomp.Responses.CONNECTED)) {
                throw new Exception ("Not connected");
            }
            */
            connection.begin("tx1");
            for (int i = 0; i < count; i++)
                connection.send((topic ? "/topic/" : "/queue/") + queue, msg, "tx1", null);
            connection.commit("tx1");

        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            connection.close();
        }
        return null;
    }
}
