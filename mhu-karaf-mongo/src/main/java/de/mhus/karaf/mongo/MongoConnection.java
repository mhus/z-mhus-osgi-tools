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
package de.mhus.karaf.mongo;

import com.mongodb.MongoClient;

import de.mhus.lib.core.MLog;

public class MongoConnection extends MLog implements MongoDataSource {

	private String name;
	private String host;
	private int port;
	private MongoClient client;

	public MongoConnection(String name, String host, int port) {
		super();
		this.name = name;
		this.host = host;
		this.port = port;
	}

	@Override
	public synchronized MongoClient getConnection() {
		if (client == null) {
			log().i("Connect",name);
			client = new MongoClient(host, port);
		}
		return client;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isConnected() {
		return client != null;
	}
	
	@Override
	public void reset() {
		if (client != null)
			client.close();
		client = null;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}
	
}
