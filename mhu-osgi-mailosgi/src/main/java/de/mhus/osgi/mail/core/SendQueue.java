/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.mail.core;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;

public interface SendQueue {

	/**
	 * Return a unique name of the service.
	 * @return x
	 */
	String getName();
	
	/**
	 * Return connection and behavior properties.
	 * @return x
	 */
	Properties getProperties();
	
	/**
	 * Set connection and behavior properties.
	 * @param properties
	 */
	void setProperties(Properties properties) throws Exception;
	
	/**
	 * Reset the connection. Maybe needed after setting of properties.
	 */
	void reset();
	
	/**
	 * Return the current status as string.
	 * @return x
	 */
	String getStatus();
	
	/**
	 * Return true if the connection is working.
	 * @return x
	 */
	boolean isValid();
	
	/**
	 * Send a mail message.
	 * 
	 * @param message
	 * @param addresses
	 */
	void sendMessage(Message message, Address[] addresses) throws Exception;
	
	Session getSession() throws Exception;
	
}
