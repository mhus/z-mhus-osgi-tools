package de.mhus.osgi.commands.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;

import de.mhus.osgi.commands.impl.AbstractDataSource;
import de.mhus.osgi.commands.impl.DataSourceUtil;


public class FailoverDataSource extends AbstractDataSource {

	private String source;
	private String realList = "";
	private BundleContext context;
	private LinkedList<DataSource> list;
	private int current = 0;
	
	@Override
	public DataSource getDataSource() throws SQLFeatureNotSupportedException {
		
		synchronized (this) {
			if (list == null) {
				current = 0;
				list = new LinkedList<DataSource>();
				realList = "";
				for (String name : source.split(",")) {
					DataSource dataSource = new DataSourceUtil(context).getDataSource(name);
					if (dataSource != null) {
						list.add(dataSource);
						if (realList.length() > 0) realList = realList + ",";
						realList = realList + name;
					}
				}
				if (list.size() == 0) {
					list = null;
					instanceName = "failover:" + source;
					return null;
				}
				instanceName = "failover:" + realList +" [" + current + "]";
			}
		}
		
		return list.get(current); 
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		SQLException lastException = null;
		while(list != null) {
			try {
				return getDataSource().getConnection(username, password);
			} catch (SQLException e) {
				if (list.isEmpty()) throw e;
				// move first connection to the end
				doDisconnect();
				lastException = e;
			}
		}
		if (lastException != null) throw lastException;
		throw new SQLException("no DataSource found",lastException);
	}

	@Override
	public void doDisconnect() {
		if (list == null) {
			instanceName = "failover:" + source;
			return;
		}
		// do a failover
		// current = (current+1) % list.size();
		current = current + 1;
		if (current >= list.size()) list = null; // disconnect
		instanceName = "failover:" + realList +" [" + current + "]";
		
	}

	@Override
	public boolean isInstanceConnected() {
		return list != null;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
		instanceName = "failover:" + source;
	}

	public BundleContext getContext() {
		return context;
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}


}
