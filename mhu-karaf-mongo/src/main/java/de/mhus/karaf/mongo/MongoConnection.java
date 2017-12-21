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
