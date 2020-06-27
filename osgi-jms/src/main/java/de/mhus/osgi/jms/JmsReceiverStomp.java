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

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;

import de.mhus.osgi.api.jms.JmsReceiver;
import de.mhus.osgi.api.jms.JmsReceiverAdmin;

public class JmsReceiverStomp implements JmsReceiver {

    private String user;
    private String password;
    private String url;
    private boolean topic;
    private String queue;

    @SuppressWarnings("unused")
    private JmsReceiverAdmin admin;

    private StompConnection connection;
    private Timer timer;

    public JmsReceiverStomp(String user, String password, String url, boolean topic, String queue) {
        this.user = user;
        this.password = password;
        this.url = url;
        this.topic = topic;
        this.queue = queue;
    }

    @Override
    public void init(JmsReceiverAdmin admin) {
        this.admin = admin;

        try {
            connection = new StompConnection();
            URL u = new URL(url);
            connection.open(u.getHost(), u.getPort());

            connection.connect(user, password);

            timer = new Timer(true);
            timer.schedule(
                    new TimerTask() {

                        @Override
                        public void run() {
                            doReceive();
                        }
                    },
                    1000,
                    100);

            connection.subscribe(
                    (topic ? "/topic/" : "/queue/") + queue, Subscribe.AckModeValues.CLIENT);
            //			connection.subscribe((topic ? "/topic/" : "/queue/") + queue,
            // Subscribe.AckModeValues.AUTO);

            System.out.println("--- " + getName() + " Listening");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void doReceive() {
        try {
            while (true) {
                StompFrame msg = connection.receive(1000);
                if (msg == null) return;
                System.out.println("--- " + getName() + " Received: " + msg.getBody());
                connection.ack(msg);
            }
        } catch (SocketTimeoutException e) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void close() {

        try {
            timer.cancel();
            connection.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println("--- " + getName() + " Closed");
    }

    @Override
    public String getName() {
        return "stomp:/" + (topic ? "topic/" : "queue/") + queue;
    }
}
