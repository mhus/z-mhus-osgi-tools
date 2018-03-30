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
package de.mhus.osgi.commands.db;

import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.karaf.DataSourceUtil;
import de.mhus.osgi.commands.impl.AbstractDataSource;


public class PoolDataSource extends AbstractDataSource {

	private String source;
	private BundleContext context;
	private DataSource dataSource;
	
	@Override
	public DataSource getDataSource() throws SQLFeatureNotSupportedException {
		
		synchronized (this) {
			if (dataSource == null) {
				
				if (context == null)
					context = FrameworkUtil.getBundle(DataSource.class).getBundleContext();

				DataSource ds = new DataSourceUtil(context).getDataSource(source);
				
				@SuppressWarnings({ "rawtypes", "unchecked" })
				ObjectPool connectionPool = new GenericObjectPool(null);
				ConnectionFactory connectionFactory = new DataSourceConnectionFactory(ds);
		        @SuppressWarnings("unused")
				PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
		        dataSource = new PoolingDataSource(connectionPool);

				// dbcp2
		    	//		ConnectionFactory connectionFactory = new DataSourceConnectionFactory(ds);
		    	//		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory();
		    	//		ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);
		    	//        poolableConnectionFactory.setPool(connectionPool);
		    	//        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<PoolableConnection>(connectionPool);
		        
			}
		}
		
		return dataSource;
	}

	@Override
	public void doDisconnect() {
		dataSource = null;
	}

	@Override
	public boolean isInstanceConnected() {
		return dataSource != null;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
		instanceName = "pool:" + source;
	}

	public BundleContext getContext() {
		return context;
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}


}
