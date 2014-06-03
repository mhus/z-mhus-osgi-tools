package de.mhus.osgi.commands.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import de.mhus.osgi.commands.db.DelegatedDataSource;

public abstract class AbstractDataSource implements DataSource, DelegatedDataSource {

	protected String instanceName = "";
		
	public abstract DataSource getDataSource() throws SQLFeatureNotSupportedException;
	
	public PrintWriter getLogWriter() throws SQLException {
		return getDataSource().getLogWriter();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		getDataSource().setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		getDataSource().setLoginTimeout(seconds);
	}

	public int getLoginTimeout() throws SQLException {
		return getDataSource().getLoginTimeout();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return getDataSource().getParentLogger();
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return getDataSource().unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return getDataSource().isWrapperFor(iface);
	}

	public Connection getConnection() throws SQLException {
		return new DelegateConnection(getDataSource().getConnection(), this);
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		try {
			return getDataSource().getConnection();
		} catch (SQLException e) {
			doDisconnect();
			throw e;
		}
	}

	public abstract void doDisconnect();

	public String getInstanceName() {
		return instanceName;
	}
	
	public abstract boolean isInstanceConnected();

	public String getDelegateURL() {
		return getInstanceName() + (!isInstanceConnected() ? " (disconnected)" : "");
	}

}