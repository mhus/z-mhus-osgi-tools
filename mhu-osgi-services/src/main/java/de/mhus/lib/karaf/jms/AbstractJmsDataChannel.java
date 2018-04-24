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

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.jms.JmsChannel;
import de.mhus.lib.jms.JmsConnection;

public abstract class AbstractJmsDataChannel extends MLog implements JmsDataChannel {

	private JmsChannel channel;
	protected String name;
	protected String connectionName;
	
	public AbstractJmsDataChannel() {
		name = getClass().getSimpleName();
	}
	
	@Override
	public JmsChannel getChannel() {
		try {
			checkChannel();
		} catch (JMSException e) {
			log().d(getName(),getConnectionName(),e);
		}
		return channel;
	}
	
	public synchronized void checkChannel() throws JMSException {
		if (channel == null)
			channel = createChannel();
	}
	
	/**
	 * Create a new channel object of the handling channel.
	 * 
	 * @return
	 * @throws JMSException
	 */
	protected abstract JmsChannel createChannel() throws JMSException;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getConnectionName() {
		return connectionName;
	}

	@Override
	public void reset() {
		log().d("reset",getName(),getConnectionName());
		onDisconnect();
		channel = null;
		onConnect();
	}

	@Override
	public void onConnect() {
		JmsConnection con = JmsUtil.getConnection(getConnectionName());
		if (con != null) {
			if (!con.isConnected())
				try {
					con.open();
				} catch (JMSException e1) {
					log().w(getName(),getConnectionName(),e1);
					return;
				}
			if (!con.isConnected()) {
				log().d("not connected",getConnectionName());
				return;
			}
			if (channel == null) {
				try {
					checkChannel();
					channel.getJmsDestination().setConnection(con);
					channel.open();
				} catch (JMSException e) {
					log().w(getName(),getConnectionName(),e);
				}
			} else {
				channel.getJmsDestination().setConnection(con);
				try {
					channel.reopen();
				} catch (JMSException e) {
					log().w(getName(),getConnectionName(),e);
				}
			}
		} else {
			log().d("connection not found", getName(), getConnectionName());
		}
	}

	@Override
	public void onDisconnect() {
		if (channel == null) return;
		channel.close();
	}
	
	@Override
	public void doBeat() {
		if (channel == null || channel.isClosed()) {
			onConnect();
			channel.isConnected();
		} else 
		if (!channel.isClosed() && !channel.isConnected()) {
			MApi.lookup(JmsManagerService.class).resetConnection(getConnectionName());
			onConnect();
		}
	}

}
