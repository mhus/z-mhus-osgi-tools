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

import javax.mail.Address;
import javax.mail.Message;

public interface SendQueueManager {

	public static String QUEUE_DEFAULT = "default";
	
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
	 * @return x
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
	 * @return x
	 */
	String[] getQueueNames();

	void sendMail(Message message, Address[] addresses) throws Exception;

	void sendMail(Message message, Address addresse) throws Exception;
	
}
