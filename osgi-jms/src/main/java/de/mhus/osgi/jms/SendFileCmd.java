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

import java.io.File;
import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.BlobMessage;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MStopWatch;

@Command(scope = "jms", name = "direct-sendfile", description = "send file")
@Service
public class SendFileCmd implements Action {

    private static final Boolean NON_TRANSACTED = false;

    @Argument(index = 0, name = "url", required = true, description = "...", multiValued = false)
    String url;

    @Argument(index = 1, name = "queue", required = true, description = "...", multiValued = false)
    String queue;

    @Argument(index = 2, name = "file", required = true, description = "...", multiValued = false)
    String file;

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
            name = "-b",
            aliases = "--blob",
            description = "Use blob message instead of byte message",
            required = false)
    boolean useBlob = false;

    @Override
    public Object execute() throws Exception {

        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(user, password, url);
        Connection connection = null;

        try {

            connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            Destination destination =
                    topic ? session.createTopic(queue) : session.createQueue(queue);
            MessageProducer producer = session.createProducer(destination);

            File f = new File(file);

            MStopWatch watch = new MStopWatch();
            watch.start();
            send(session, producer, f);
            watch.stop();
            System.out.println(watch.getCurrentTimeAsString());

            producer.close();
            session.close();

        } catch (Exception e) {
            System.out.println("Caught exception!");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    System.out.println("Could not close an open connection...");
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void send(Session session, MessageProducer producer, File f)
            throws JMSException, IOException {
        if (f.isDirectory()) {
            for (File g : f.listFiles()) {
                if ((g.isDirectory() || g.isFile())
                        && !g.isHidden()
                        && !g.getName().startsWith(".")) {
                    send(session, producer, g);
                }
            }
            return;
        }
        if (f.isFile()) {
            System.out.println("Send " + f.getName() + " " + f.length());
            if (useBlob) {
                BlobMessage message = ((ActiveMQSession) session).createBlobMessage(f);
                message.setStringProperty("filename", f.getName());
                message.setLongProperty("filesize", f.length());
                producer.send(message);
            } else {
                BytesMessage message = session.createBytesMessage();
                message.setStringProperty("filename", f.getName());
                message.setLongProperty("filesize", f.length());
                byte[] b = MFile.readBinaryFile(f);
                message.writeBytes(b);
                producer.send(message);
            }
        }
    }
}
