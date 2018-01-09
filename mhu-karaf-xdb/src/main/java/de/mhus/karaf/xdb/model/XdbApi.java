package de.mhus.karaf.xdb.model;

import java.util.List;

import de.mhus.lib.errors.NotFoundException;

public interface XdbApi {

	XdbService getService(String serviceName) throws NotFoundException;

	<T> XdbType<T> getType(String serviceName, String typeName) throws NotFoundException;

	List<String> getServiceNames();

}
