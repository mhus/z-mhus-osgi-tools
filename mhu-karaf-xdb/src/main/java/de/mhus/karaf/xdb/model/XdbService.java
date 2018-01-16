package de.mhus.karaf.xdb.model;

import java.util.List;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.errors.NotFoundException;

public interface XdbService {

	boolean isConnected();

	List<String> getTypeNames();
	
	<T> XdbType<T> getType(String name) throws NotFoundException;

	String getSchemaName();

	String getDataSourceName();

	void updateSchema(boolean cleanup) throws Exception;

	void connect() throws Exception;

	default <T> T getObjectByQualification(AQuery<T> query) throws Exception {
		XdbType<T> type = getType(query.getType());
		return type.getObjectByQualification(query);
	}

	default <T> DbCollection<T> getByQualification(AQuery<T> query) throws Exception {
		XdbType<T> type = getType(query.getType());
		return type.getByQualification(query);
	}

	<T> XdbType<T> getType(Class<?> type) throws NotFoundException;

}
