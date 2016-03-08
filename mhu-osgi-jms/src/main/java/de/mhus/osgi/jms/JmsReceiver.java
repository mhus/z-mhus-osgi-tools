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

public interface JmsReceiver {

	public void init(JmsReceiverAdmin admin);

	void close();
	String getName();
	
}
