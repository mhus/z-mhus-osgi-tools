package de.mhus.karaf.mongo;

import java.util.List;

import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.MOsgi;

public class MongoUtil {

	public static MongoDataSource getDatasource(String name) throws NotFoundException {
		return MOsgi.getService(MongoDataSource.class, "(lookup.name=" + name + ")");
	}

	public static List<MoManagerService> getManagerServices() {
		return MOsgi.getServices(MoManagerService.class, null);
	}
		
}
