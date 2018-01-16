package de.mhus.karaf.mongo;

import de.mhus.lib.errors.MException;
import de.mhus.lib.mongo.MoManager;

public interface MoManagerService {

	void doOpen() throws MException;
	
	void doInitialize();

	void doClose();

	String getServiceName();

	MoManager getManager();

	String getMongoDataSourceName();
	
	boolean isConnected();
	
}
