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

import java.util.List;

import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.karaf.MOsgi.Service;

public interface JmsManagerService {
	
//	void addConnection(String name, JmsConnection con);
//	void addConnection(String name, String url, String user, String password) throws JMSException;
//	String[] listConnections();
	JmsConnection getConnection(String name);
//	void removeConnection(String name);
//	String[] listChannels();
	JmsDataChannel getChannel(String name);
	void addChannel(JmsDataChannel channel);
	void removeChannel(String name);
	void resetChannels();
	void doChannelBeat();
	List<JmsDataChannel> getChannels();
	List<JmsConnection> getConnections();
	List<Service<JmsDataSource>> getDataSources();
	String getServiceName(de.mhus.lib.karaf.MOsgi.Service<JmsDataSource> ref);
	void doBeat();
	
	/**
	 * Called if the connection is offline to inform all the channels to disconnect from the connection.
	 * 
	 * @param connectionName
	 */
	void resetConnection(String connectionName);

}
