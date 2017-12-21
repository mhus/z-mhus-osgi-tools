package de.mhus.karaf.mongo;

import de.mhus.lib.errors.NotFoundException;

public interface MoManagerAdmin {

	void addService(MoManagerService service) throws Exception;

	void removeService(MoManagerService service);

	MoManagerService getService(String name) throws NotFoundException;

}
