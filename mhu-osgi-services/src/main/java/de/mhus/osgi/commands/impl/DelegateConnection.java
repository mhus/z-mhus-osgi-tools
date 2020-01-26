/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
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

    public DelegateConnection(Connection connection, DelegatedDataSource dataSource) {
        instance = connection;
        this.dataSource = dataSource;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return instance.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return instance.isWrapperFor(iface);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return instance.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return instance.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return instance.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return instance.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        instance.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return instance.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        instance.commit();
    }

    @Override
    public void rollback() throws SQLException {
        instance.rollback();
    }

    @Override
    public void close() throws SQLException {
        instance.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return instance.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return new DelegateMetaData(instance.getMetaData(), dataSource);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        instance.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return instance.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        instance.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return instance.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        instance.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return instance.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return instance.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        instance.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return instance.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(
            String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return instance.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return instance.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return instance.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        instance.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        instance.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return instance.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return instance.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return instance.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        instance.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        instance.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(
            int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return instance.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(
            String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return instance.prepareStatement(
                sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(
            String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return instance.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
        return instance.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return instance.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException {
        return instance.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return instance.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return instance.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return instance.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return instance.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return instance.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        instance.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        instance.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return instance.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return instance.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return instance.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return instance.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        instance.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return instance.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        instance.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        instance.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return instance.getNetworkTimeout();
    }

    public DelegatedDataSource getDataSource() {
        return dataSource;
    }
}
