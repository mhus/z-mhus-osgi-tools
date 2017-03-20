package de.mhus.osgi.jms.util;

import javax.jms.JMSException;
import javax.jms.Message;

import de.mhus.lib.core.logging.Log;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.ServerJms;

public class DLQConsumer extends ServerJms {

	Log log = Log.getLog(DLQConsumer.class);
	
	public DLQConsumer(JmsConnection con) {
		super(new JmsDestination("ActiveMQ.DLQ", false).setConnection(con));
	}

	@Override
	public void receivedOneWay(Message msg) throws JMSException {
		log.w("Lost Message",msg);
	}

	@Override
	public Message received(Message msg) throws JMSException {
		receivedOneWay(msg);
		return null;
	}

	
}
