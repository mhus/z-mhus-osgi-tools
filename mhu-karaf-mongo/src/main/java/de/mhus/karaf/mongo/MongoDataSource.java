package de.mhus.karaf.mongo;

import com.mongodb.MongoClient;

import de.mhus.lib.basics.Named;

public interface MongoDataSource extends Named {

	int getPort();

	boolean isConnected();

	void reset();

	String getHost();

	MongoClient getConnection();

}
