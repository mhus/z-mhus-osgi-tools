package de.mhus.karaf.mongo;

import java.util.List;

import com.mongodb.MongoClient;

import de.mhus.lib.adb.Persistable;
import de.mhus.lib.core.jmx.MJmx;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.mongo.MoManager;
import de.mhus.lib.mongo.MoSchema;

//@Component(immediate=true,provide=MoManagerService.class)
public abstract class MoManagerServiceImpl extends MJmx implements MoManagerService, Comparable<MoManagerService> {

	private MoManager manager;

	@Override
	public abstract void doInitialize();
	
	@Override
	public void doClose() {
		if (manager == null) return;
		manager.close();
		manager = null;
	}

	@Override
	public abstract String getServiceName();

	@Override
	public synchronized void doOpen() throws MException {
		if (manager != null) return;
		doInitialize();
		manager = createManager();
	}

	@Override
	public MoManager getManager() {
		try {
			doOpen();
		} catch (Throwable t) {
			log().e(t);
		}
		return manager;
	}

	protected MoManager createManager() throws NotFoundException {
		 MongoDataSource ds = getMongoDataSource();
		 MongoClient client = ds.getConnection();
		 MoSchema schema = doCreateSchema();
		 
		 MoManager manager = new MoManager(client,schema);
		 
		return manager;
	}

	protected MoSchema doCreateSchema() {
		return new MoSchema() {
			
			@Override
			public String getDatabaseName() {
				return MoManagerServiceImpl.this.getServiceName();
			}
			
			@Override
			public void findObjectTypes(List<Class<? extends Persistable>> list) {
				MoManagerServiceImpl.this.findObjectTypes(list);
			}
		};
	}
	
	protected MongoDataSource getMongoDataSource() throws NotFoundException {
		String name = getMongoDataSourceName();
		MongoDataSource ds = MongoUtil.getDatasource(name);
		return ds;
	}

	@Override
	public abstract String getMongoDataSourceName();
	
	protected abstract void findObjectTypes(List<Class<? extends Persistable>> list);

    @Override
	public int compareTo(MoManagerService o) {
    	if (o == null || getServiceName() == null) return -1;
    	return getServiceName().compareTo(o.getServiceName());
    }

}
