package de.mhus.karaf.xdb.model;

import java.util.List;

import de.mhus.lib.errors.NotFoundException;

public interface XdbService {

	boolean isConnected();

	List<String> getTypeNames();
	
	<T> XdbType<T> getType(String name) throws NotFoundException;

	String getSchemaName();

	String getDataSourceName();

	void updateSchema(boolean cleanup) throws Exception;

	void connect() throws Exception;

}
