package de.mhus.osgi.commands.db;

import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.mhus.osgi.commands.impl.AbstractDataSource;
import de.mhus.lib.karaf.DataSourceUtil;


public class DelegateDataSource extends AbstractDataSource {

	private String source;
	private BundleContext context;
	private DataSource dataSource;
	
	@Override
	public DataSource getDataSource() throws SQLFeatureNotSupportedException {
		
		synchronized (this) {
			
			if (context == null)
				context = FrameworkUtil.getBundle(DataSource.class).getBundleContext();
			
			if (dataSource == null) {
				dataSource = new DataSourceUtil(context).getDataSource(source);
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
		instanceName = "delegate:" + source;
	}

	public BundleContext getContext() {
		return context;
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}


}
