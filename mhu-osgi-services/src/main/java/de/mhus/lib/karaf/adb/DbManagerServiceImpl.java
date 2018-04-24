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
package de.mhus.lib.karaf.adb;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbManagerJdbc;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.core.MActivator;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.activator.DefaultActivator;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.errors.MException;
import de.mhus.lib.karaf.DataSourceUtil;
import de.mhus.lib.sql.DataSourceProvider;
import de.mhus.lib.sql.DbPool;
import de.mhus.lib.sql.DefaultDbPool;
import de.mhus.lib.sql.Dialect;
//@Component(provide=DbManagerService.class,name="...",immediate=true)
public abstract class DbManagerServiceImpl extends MLog implements DbManagerService {

	protected DataSourceUtil util;
	private String dataSourceName;
	private DbManager manager;

//	protected abstract void doInitialize() throws Exception;
	
	/**
	 * Call this function in the doActivate() after you set the context and dataSourceName attribute.
	 * @throws Exception 
	 */
	protected void doOpen(boolean clean) throws MException {
		if (manager != null) return;
		doInitialize();
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
		util = new DataSourceUtil(context);
		
		if (getDataSource() == null) return;

		manager = doCreateDbManager(clean);
		
		doPostOpen();
	}
	
	protected void doPostOpen() throws MException {
	}
	
	@Override
	public void doClose() {
		if (manager == null) return;
		manager.getPool().close();
		manager = null;
	}
	
	protected DbManager doCreateDbManager(boolean clean) throws MException {
		
		DbPool pool = doCreateDataPool();
		DbSchema schema = doCreateSchema();
		return new DbManagerJdbc(dataSourceName, pool, schema, clean); //TODO configurable
	}

	protected abstract DbSchema doCreateSchema();

	protected DbPool doCreateDataPool() {
		return new DefaultDbPool(new DataSourceProvider(getDataSource(), doCreateDialect(), doCreateConfig(), doCreateActivator() ));
	}

	protected MActivator doCreateActivator() {
		try {
			return new DefaultActivator(null, getClass().getClassLoader());
		} catch (MException e) {
			log().e(e);
		}
		return null;
	}

	protected IConfig doCreateConfig() {
		return null;
	}

	protected Dialect doCreateDialect() {
		return null;
	}

	protected DataSource getDataSource() {
		DataSource ds = util.getDataSource(dataSourceName);
		if (ds == null)
			log().w("DataSource is unknown",dataSourceName);
		return ds;
	}
	
	@Override
	public void updateManager(boolean clean) throws MException {
		doClose();
//		if (!isConnected()) {
			if (getDataSource() == null) return;
			doOpen(clean);
//			return;
//		}
//		((DataSourceProvider)manager.getPool().getProvider()).setDataSource(getDataSource());
	}
	
	@Override
	public DbManager getManager() {
		try {
			doOpen(false);
		} catch (Exception e) {
			log().d(e);
		}
		return manager;
	}
	
	@Override
	public boolean isConnected() {
		return manager != null;
	}

	@Override
	public String getDataSourceName() {
		return dataSourceName;
	}

	@Override
	public void setDataSourceName(String dataSourceName) {
		if (MSystem.equals(this.dataSourceName, dataSourceName))
			return;
		this.dataSourceName = dataSourceName;
		try {
			updateManager(false);
		} catch (Exception e) {
		}
	}
	
	@Override
	public String getServiceName() {
		return getClass().getSimpleName();
	}

}
