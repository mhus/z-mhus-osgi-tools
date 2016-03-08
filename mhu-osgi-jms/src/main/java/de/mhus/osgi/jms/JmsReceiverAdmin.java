package de.mhus.osgi.jms;

public interface JmsReceiverAdmin {

	void add(JmsReceiver receiver);
	void remove(String queue);
	JmsReceiver[] list();
}
