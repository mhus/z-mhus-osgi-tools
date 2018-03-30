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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import de.mhus.osgi.commands.db.DelegatedDataSource;

public class DelegateConnection implements Connection {
	
	protected Connection instance;
	protected DelegatedDataSource dataSource;

	public DelegateConnection(Connection connection,
			DelegatedDataSource dataSource) {
		instance = connection;
		this.dataSource = dataSource;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return instance.unwrap(iface);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return instance.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		return instance.createStatement();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return instance.prepareStatement(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return instance.prepareCall(sql);
	}

	public String nativeSQL(String sql) throws SQLException {
		return instance.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		instance.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit() throws SQLException {
		return instance.getAutoCommit();
	}

	public void commit() throws SQLException {
		instance.commit();
	}

	public void rollback() throws SQLException {
		instance.rollback();
	}

	public void close() throws SQLException {
		instance.close();
	}

	public boolean isClosed() throws SQLException {
		return instance.isClosed();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return new DelegateMetaData( instance.getMetaData(), dataSource);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		instance.setReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		return instance.isReadOnly();
	}

	public void setCatalog(String catalog) throws SQLException {
		instance.setCatalog(catalog);
	}

	public String getCatalog() throws SQLException {
		return instance.getCatalog();
	}

	public void setTransactionIsolation(int level) throws SQLException {
		instance.setTransactionIsolation(level);
	}

	public int getTransactionIsolation() throws SQLException {
		return instance.getTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		return instance.getWarnings();
	}

	public void clearWarnings() throws SQLException {
		instance.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return instance.createStatement(resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return instance.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return instance.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return instance.getTypeMap();
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		instance.setTypeMap(map);
	}

	public void setHoldability(int holdability) throws SQLException {
		instance.setHoldability(holdability);
	}

	public int getHoldability() throws SQLException {
		return instance.getHoldability();
	}

	public Savepoint setSavepoint() throws SQLException {
		return instance.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return instance.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		instance.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		instance.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return instance.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return instance.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return instance.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return instance.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return instance.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return instance.prepareStatement(sql, columnNames);
	}

	public Clob createClob() throws SQLException {
		return instance.createClob();
	}

	public Blob createBlob() throws SQLException {
		return instance.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return instance.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return instance.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return instance.isValid(timeout);
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		instance.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		instance.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return instance.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return instance.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return instance.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return instance.createStruct(typeName, attributes);
	}

	public void setSchema(String schema) throws SQLException {
		instance.setSchema(schema);
	}

	public String getSchema() throws SQLException {
		return instance.getSchema();
	}

	public void abort(Executor executor) throws SQLException {
		instance.abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		instance.setNetworkTimeout(executor, milliseconds);
	}

	public int getNetworkTimeout() throws SQLException {
		return instance.getNetworkTimeout();
	}

	public DelegatedDataSource getDataSource() {
		return dataSource;
	}

}
