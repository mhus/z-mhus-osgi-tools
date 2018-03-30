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
package de.mhus.osgi.commands.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.sql.DataSource;

import de.mhus.osgi.commands.db.DelegatedDataSource;

public class DataSourceFailOver implements DataSource, DelegatedDataSource {

	private LinkedList<DataSource> list;

	public DataSource getCurrentDataSource() {
		if (list.isEmpty())
			return null;
		return list.get(0); // implement fallback 
	}
	
	public PrintWriter getLogWriter() throws SQLException {
		return getCurrentDataSource().getLogWriter();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		getCurrentDataSource().setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		getCurrentDataSource().setLoginTimeout(seconds);
	}

	public int getLoginTimeout() throws SQLException {
		return getCurrentDataSource().getLoginTimeout();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return getCurrentDataSource().getParentLogger();
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return getCurrentDataSource().unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return getCurrentDataSource().isWrapperFor(iface);
	}

	public Connection getConnection() throws SQLException {
		return getCurrentDataSource().getConnection();
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		SQLException lastException = null;
		for( int i = 0; i < list.size(); i++) {
			try {
				return getCurrentDataSource().getConnection(username, password);
			} catch (SQLException e) {
				if (list.isEmpty()) throw e;
				// move first connection to the end
				DataSource ds = list.removeFirst();
				list.addLast(ds);
				lastException = e;
			}
		}
		if (lastException != null) throw lastException;
		throw new SQLException("no DataSource found");
	}

	public void setDestinations(LinkedList<DataSource> list) {
		this.list = list;
	}

	public String getDelegateURL() {
		try {
			return "failover:" + getCurrentDataSource().getConnection().getMetaData().getURL();
		} catch (Throwable e) {
			return "failover:" + e;
		}
	}

}
