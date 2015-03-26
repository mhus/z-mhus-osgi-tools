package de.mhus.osgi.jms;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/*
 http://activemq.apache.org/how-should-i-implement-request-response-with-jms.html
 
 */

public class JmsReceiverOpenWire implements ExceptionListener, MessageListener, JmsReceiver {

	private String user;
	private String password;
	private String url;
	private Session session;
	private MessageProducer replyProducer;
	private boolean topic;
	private String queue;
	private JmsReceiverAdmin admin;
	private MessageConsumer consumer;
	private Connection connection;

	public JmsReceiverOpenWire(String user, String password, String url, boolean topic, String queue) {
		this.user = user;
		this.password = password;
		this.url = url;
		this.topic = topic;
		this.queue = queue;
	}
		
	public void init(JmsReceiverAdmin admin) {
		this.admin = admin;
		try {

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password,url);

            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();

            connection.setExceptionListener(this);

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            this.replyProducer = session.createProducer(null);
            this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            // Create the destination (Topic or Queue)
//            Destination destination = session.createTopic("event");
            Destination destination = topic ? session.createTopic(queue) : session.createQueue(queue);

            // Create a MessageConsumer from the Session to the Topic or Queue
            consumer = session.createConsumer(destination);

			System.out.println("--- " + getName() + " Listening");
            
            consumer.setMessageListener(this);
/*            
            while (true) {
	            // Wait for a message
	            Message message = consumer.receive(1000);
	            if (message == null) continue;
	            
	            if (message instanceof TextMessage) {
	                TextMessage textMessage = (TextMessage) message;
	                String text = textMessage.getText();
	                System.out.println("Received: " + text);
	                if ("SHUTDOWN".equals(text)) {
	    	            reply(message,"DONE");
	                	break;
	                }
	            } else
	            if (message instanceof BytesMessage) {
	            	long len = ((BytesMessage)message).getBodyLength();
	                System.out.println("Received byte message " + message.getStringProperty("filename") + " " + len );
	            } else {
	                System.out.println("Received: " + message);
	            }
	            
	            reply(message,"OK");
            }
*/            
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
		
		
	}

	public void close() {
		try {
	        consumer.close();
	        if (replyProducer != null) replyProducer.close();
	        session.close();
	        connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		System.out.println("--- " + getName() + " Closed");
	        
	}
	
	public void onException(JMSException arg0) {
		// TODO Auto-generated method stub
		
	}

	public void reply(Message message, String msg) throws JMSException {
		if (message.getJMSReplyTo() == null) return;
        try {
            TextMessage response = session.createTextMessage();
            response.setText(msg);
 
            //Set the correlation ID from the received message to be the correlation id of the response message
            //this lets the client identify which message this is a response to if it has more than
            //one outstanding message to the server
            response.setJMSCorrelationID(message.getJMSCorrelationID());
 
            //Send the response to the Destination specified by the JMSReplyTo field of the received message,
            //this is presumably a temporary queue created by the client
            replyProducer.send(message.getJMSReplyTo(), response);
        } catch (JMSException e) {
            //Handle the exception appropriately
        	e.printStackTrace();
        }
    }

	public void onMessage(Message message) {
		try {
	        if (message == null) return;
	        
	        if (message instanceof TextMessage) {
	            TextMessage textMessage = (TextMessage) message;
	            String text = textMessage.getText();
	            System.out.println("--- " + getName() + " Received: " + text);
//	            if ("SHUTDOWN".equals(text)) {
//	            	admin.remove(getName());
//		            reply(message,"DONE");
//	            	return;
//	            }
	        } else
	        if (message instanceof BytesMessage) {
	        	long len = ((BytesMessage)message).getBodyLength();
	            System.out.println("--- " + getName() + " Received byte message " + message.getStringProperty("filename") + " " + len );
	        } else {
	            System.out.println("--- " + getName() + " Received: " + message);
	        }
	        
	        reply(message,"OK");
        } catch (JMSException e) {
            //Handle the exception appropriately
        	e.printStackTrace();
        }
	}

	public String getName() {
		return (topic ? "jms:/topic/" : "queue/") + queue;
	}
}
