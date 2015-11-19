package de.mhus.osgi.mail.core;

import javax.mail.Address;
import javax.mail.Message;

public interface SendQueueManager {

	/**
	 * Send a mail.
	 * 
	 * @param queue
	 * @param message
	 * @param addresse
	 * @throws Exception
	 */
	void sendMail(String queue, Message message, Address addresse) throws Exception;

	/**
	 * Send a mail.
	 * 
	 * @param queue
	 * @param message
	 * @param adresses
	 * @throws Exception 
	 */
	void sendMail(String queue, Message message, Address[] addresses) throws Exception;
	
	/**
	 * Get a queue.
	 * 
	 * @param name
	 * @return
	 */
	SendQueue getQueue(String name);
	
	/**
	 * Register a new queue or overwrite a queue.
	 * 
	 * @param queue
	 */
	void registerQueue(SendQueue queue);
	
	/**
	 * Unregister a existing queue.
	 * 
	 * @param name
	 */
	void unregisterQueue(String name);
	
	/**
	 * Return a list of queue names.
	 * 
	 * @return
	 */
	String[] getQueueNames();

	void sendMail(Message message, Address[] addresses) throws Exception;

	void sendMail(Message message, Address addresse) throws Exception;
	
}
