package de.mhus.osgi.jms;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.MStopWatch;

/*
MESSAGE
destination:/queue/a
message-id: <message-identifier>

hello queue a^@
            	String m = msg + " " + i;
            	String frame = "MESSAGE\n" + "destination:/topic/" + queue + "\nmessage-id:" + UUID.randomUUID() + "\n\n" + m + Stomp.NULL;

 */

@Command(scope = "jms", name = "direct-send", description = "send")
public class SendCmd implements Action {

    private static final Boolean NON_TRANSACTED = false;
    private static final long DELAY = 100;
    
	@Argument(index=0, name="url", required=true, description="...", multiValued=false)
    String url;
	
	@Argument(index=1, name="queue", required=true, description="...", multiValued=false)
    String queue;

	@Argument(index=2, name="msg", required=true, description="...", multiValued=false)
    String msg;

	@Argument(index=3, name="count", required=false, description="...", multiValued=false)
    int count = 1;
	
	@Option(name="-t", aliases="--topic", description="Use a topic instead of a queue",required=false)
	boolean topic = false;
	
	@Option(name="-s", aliases="--synchronized", description="Create a temporary answer queue and wait for the answer",required=false)
	boolean sync = false;
	
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

            TemporaryQueue answerQueue = null;
            MessageConsumer responseConsumer = null;
            if (sync) {
            	answerQueue = session.createTemporaryQueue();
            	responseConsumer = session.createConsumer(answerQueue);
            }
            
            MStopWatch watch = new MStopWatch();
            watch.start();
            for (int i = 0; i < count; i++) {
                TextMessage message = session.createTextMessage(msg);
                String id = UUID.randomUUID().toString();
                message.setJMSMessageID(id);
                if (answerQueue != null) {
                	message.setJMSReplyTo(answerQueue);
                	message.setJMSCorrelationID(id);
                }
                System.out.println("Sending message #" + i);
                producer.send(message);
                if (answerQueue != null) {
                	Message answer = responseConsumer.receive(1000 * 30); // wait 30 seconds
                	if (answer == null) {
                		System.out.println("*** Answer not received");
                	} else {
                		String answerTxt = null;
                		if (answer instanceof TextMessage)
                			answerTxt = ((TextMessage)answer).getText();
                		else
                			answerTxt = answer.toString();
                		System.out.println("--- Answer: " + watch.getCurrentTimeAsString(true) + " " + answerTxt);
                	}
                }
            }
            watch.stop();
            System.out.println(watch.getCurrentTimeAsString(true));
            // tell the subscribers we're done
//            producer.send(session.createTextMessage("END"));

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
	
}
