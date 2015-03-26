package de.mhus.osgi.jms;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.stomp.Stomp;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MStopWatch;

@Command(scope = "jms", name = "file", description = "send")
public class SendFileCmd implements Action {

    private static final Boolean NON_TRANSACTED = false;
    
	@Argument(index=0, name="url", required=true, description="...", multiValued=false)
    String url;
	
	@Argument(index=1, name="queue", required=true, description="...", multiValued=false)
    String queue;

	@Argument(index=2, name="file", required=true, description="...", multiValued=false)
    String file;
	
	@Option(name="-t", aliases="--topic", description="Use a topic instead of a queue",required=false)
	boolean topic = false;

	@Option(name="-u", aliases="--user", description="User",required=false)
	String user = "admin";
	
	@Option(name="-p", aliases="--password", description="Password",required=false)
	String password = "password";

	public Object execute(CommandSession s) throws Exception {

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
        Connection connection = null;

        try {

            connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            Destination destination = topic ? session.createTopic(queue) : session.createQueue(queue);
            MessageProducer producer = session.createProducer(destination);

            File f = new File(file);
            
            MStopWatch watch = new MStopWatch();
            watch.start();
            send(session, producer, f);
            watch.stop();
            System.out.println(watch.getCurrentTimeAsString(true));

            producer.close();
            session.close();

        } catch (Exception e) {
            System.out.println("Caught exception!");
            e.printStackTrace();
        }
        finally {
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

	private void send(Session session, MessageProducer producer, File f) throws JMSException, IOException {
		if (f.isDirectory()) {
			for (File g : f.listFiles()) {
				if ((g.isDirectory() || g.isFile()) && !g.isHidden() && !g.getName().startsWith(".")) {
					send(session,producer,g);
				}
			}
			return;
		}
		if (f.isFile()) {
			System.out.println("Send " + f.getName() + " " + f.length());
            BytesMessage message = session.createBytesMessage();
            message.setStringProperty("filename", f.getName());
            byte[] b = MFile.readBinaryFile(f);
            message.writeBytes(b);
            producer.send(message);

		}
	}
	
}
