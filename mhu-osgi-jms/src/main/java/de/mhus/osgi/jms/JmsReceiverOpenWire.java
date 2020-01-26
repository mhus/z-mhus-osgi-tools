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
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.BlobMessage;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.ServerJms;
import de.mhus.osgi.api.jms.JmsReceiver;
import de.mhus.osgi.api.jms.JmsReceiverAdmin;

/*
http://activemq.apache.org/how-should-i-implement-request-response-with-jms.html

*/

public class JmsReceiverOpenWire extends MLog implements JmsReceiver {

    private String user;
    private String password;
    private String url;

    @SuppressWarnings("unused")
    private Session session;

    private boolean topic;
    private String queue;

    @SuppressWarnings("unused")
    private JmsReceiverAdmin admin;

    private JmsConnection con;
    private ServerJms server;
    private boolean ownConnection = false;

    public JmsReceiverOpenWire(
            String user, String password, String url, boolean topic, String queue) {
        this.user = user;
        this.password = password;
        this.url = url;
        this.topic = topic;
        this.queue = queue;
    }

    public JmsReceiverOpenWire(JmsConnection con, boolean topic, String queue) {
        this.topic = topic;
        this.con = con;
        this.queue = queue;
    }

    @Override
    public void init(JmsReceiverAdmin admin) {
        this.admin = admin;
        try {

            if (con == null) {
                con = new JmsConnection(url, user, password);
                ownConnection = true;
            }
            con.open();
            session = con.getSession();
            // Create a Session
            // session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            this.server =
                    new ServerJms(topic ? con.createTopic(queue) : con.createQueue(queue)) {

                        @Override
                        public void receivedOneWay(Message msg) throws JMSException {
                            if (msg instanceof MapMessage) ((MapMessage) msg).getMapNames();
                            log().d("Received One Way", msg);
                            doProcess(msg);
                        }

                        @Override
                        public Message received(Message msg) throws JMSException {
                            if (msg instanceof MapMessage) ((MapMessage) msg).getMapNames();
                            // System.out.println("--- " + getName() + " Received: " + msg);
                            log().d("Received", msg);
                            doProcess(msg);
                            TextMessage ret = con.getSession().createTextMessage("ok");
                            return ret;
                        }
                    };

            server.open();
            log().i("Listening");

        } catch (Exception e) {
            log().e(e);
        }
    }

    protected void doProcess(Message msg) {
        try {
            if (msg instanceof BlobMessage && msg.getStringProperty("filename") != null) {
                String filename = msg.getStringProperty("filename");
                filename = filename.replace("..", "_");
                filename = filename.replace("~", "_");
                while (filename.startsWith("/")) filename = filename.substring(1);
                File file = new File("jms/" + queue + "/" + filename);
                file.getParentFile().mkdirs();
                FileOutputStream os = new FileOutputStream(file);
                BlobMessage blob = (BlobMessage) msg;
                System.out.println("Filename: " + filename);
                System.out.println("Filesize: " + msg.getLongProperty("filesize"));
                System.out.println("Blob Name: " + blob.getName());
                InputStream is = blob.getInputStream();
                MFile.copyFile(is, os);
                os.close();
                is.close();
            } else if (msg instanceof BytesMessage && msg.getStringProperty("filename") != null) {
                String filename = msg.getStringProperty("filename");
                filename = filename.replace("..", "_");
                filename = filename.replace("~", "_");
                while (filename.startsWith("/")) filename = filename.substring(1);
                System.out.println("Filename: " + filename);
                System.out.println("Filesize: " + msg.getLongProperty("filesize"));
                File file = new File("jms/" + queue + "/" + filename);
                file.getParentFile().mkdirs();
                FileOutputStream os = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                while (true) {
                    int size = ((BytesMessage) msg).readBytes(buffer);
                    if (size < 0) break;
                    if (size == 0) Thread.sleep(100);
                    else {
                        os.write(buffer, 0, size);
                    }
                }
                os.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (ownConnection) con.close();
        } catch (Exception e) {
            log().e(e);
        }
        log().i("Closed");
    }

    public void onException(JMSException arg0) {
        log().e(arg0);
    }

    @Override
    public String getName() {
        return (topic ? "jms:/topic/" : "queue/") + queue;
    }
}
