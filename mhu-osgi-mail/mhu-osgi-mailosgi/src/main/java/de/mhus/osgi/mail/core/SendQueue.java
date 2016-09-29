package de.mhus.osgi.mail.core;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;

public interface SendQueue {

	/**
	 * Return a unique name of the service.
	 * @return The name
	 */
	String getName();
	
	/**
	 * Return connection and behavior properties.
	 * @return the properties
	 */
	Properties getProperties();
	
	/**
	 * Set connection and behavior properties.
	 * @param properties TODO
	 */
	void setProperties(Properties properties) throws Exception;
	
	/**
	 * Reset the connection. Maybe needed after setting of properties.
	 */
	void reset();
	
	/**
	 * Return the current status as string.
	 * @return the status
	 */
	String getStatus();
	
	/**
	 * Return true if the connection is working.
	 * @return true it its valid
	 */
	boolean isValid();
	
	/**
	 * Send a mail message.
	 * 
	 * @param message TODO
	 * @param addresses TODO
	 */
	void sendMessage(Message message, Address[] addresses) throws Exception;
	
	Session getSession() throws Exception;
	
}
