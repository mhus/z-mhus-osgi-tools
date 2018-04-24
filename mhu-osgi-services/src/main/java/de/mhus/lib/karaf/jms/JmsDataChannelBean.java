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
package de.mhus.lib.karaf.jms;

import javax.jms.JMSException;

import de.mhus.lib.jms.JmsChannel;
import de.mhus.lib.jms.JmsDestination;

public class JmsDataChannelBean extends AbstractJmsDataChannel {

	private JmsChannel channel;
	private String destination;
	private boolean destinationTopic;
	
	public JmsDataChannelBean() {}
	
	public JmsDataChannelBean(String destination, String connectionName, JmsChannel channel) {
		name = connectionName + "/" + destination;
		this.destination = destination;
		this.connectionName = connectionName;
		setChannel(channel);
	}
	
	public JmsDataChannelBean(String destination, String connectionName, boolean destinationTopic, JmsChannel channel) {
		name = connectionName + "/" + destination;
		this.destination = destination;
		this.destinationTopic = destinationTopic;
		this.connectionName = connectionName;
		setChannel(channel);
	}
	
	@Override
	protected JmsChannel createChannel() throws JMSException {
		JmsDestination dest = new JmsDestination(destination, destinationTopic);
		channel.reset(dest);
		return channel;
	}

	public void setChannel(JmsChannel channel) {
		this.channel = channel;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public boolean isDestinationTopic() {
		return destinationTopic;
	}

	public void setDestinationTopic(boolean destinationTopic) {
		this.destinationTopic = destinationTopic;
	}

}
