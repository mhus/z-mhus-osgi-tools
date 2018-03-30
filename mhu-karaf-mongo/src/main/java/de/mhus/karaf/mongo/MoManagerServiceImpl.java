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

import java.util.List;

import org.mongodb.morphia.mapping.DefaultCreator;
import org.mongodb.morphia.mapping.Mapper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

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
	public boolean isConnected() {
		return manager != null;
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
		 
		 BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		 MoManager manager = new MoManager(client,schema) {
				@Override
				protected Mapper createMapper() {
					Mapper m = super.createMapper();
					m.getOptions().setObjectFactory(new BundleObjectFactory(bundleContext));
					return m;
				}

		 };

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

    private static class BundleObjectFactory extends DefaultCreator {
        private BundleContext bundleContext;
         
        @Override
        protected ClassLoader getClassLoaderForClass() {
            ClassLoader cl = ((BundleWiring)bundleContext.getBundle().adapt(
                BundleWiring.class)).getClassLoader();
             
            return cl;
        }
     
        public BundleObjectFactory(BundleContext bundleContext) {
            super();
            this.bundleContext = bundleContext;
        }
    }
    
}
