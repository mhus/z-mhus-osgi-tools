package de.mhus.osgi.jms;

/*
 http://activemq.apache.org/how-should-i-implement-request-response-with-jms.html
 
 */

public interface JmsReceiver {

	public void init(JmsReceiverAdmin admin);

	void close();
	String getName();
	
}
