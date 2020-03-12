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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import de.mhus.osgi.commands.db.TraceDataSource;

public class TracedStatement implements Statement {

    private Statement instance;
    private TracedConnection con;
    private TraceDataSource ds;

    public TracedStatement(Statement createStatement, TracedConnection delegateConnection) {
        instance = createStatement;
        con = delegateConnection;
        ds = (TraceDataSource) con.getDataSource();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return instance.unwrap(iface);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.executeQuery(sql);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return instance.isWrapperFor(iface);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.executeUpdate(sql);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public void close() throws SQLException {
        instance.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return instance.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        instance.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return instance.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        instance.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        instance.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return instance.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        instance.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        instance.cancel();
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
    public void setCursorName(String name) throws SQLException {
        instance.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.execute(sql);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return instance.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return instance.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return instance.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        instance.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return instance.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        instance.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return instance.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return instance.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return instance.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        instance.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        instance.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        long time = ds.startTrace("");
        try {
            return instance.executeBatch();
        } finally {
            ds.stopTrace(time, "");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return con;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return instance.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return instance.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.executeUpdate(sql, autoGeneratedKeys);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.executeUpdate(sql, columnIndexes);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.executeUpdate(sql, columnNames);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.execute(sql, autoGeneratedKeys);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.execute(sql, columnIndexes);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        long time = ds.startTrace(sql);
        try {
            return instance.execute(sql, columnNames);
        } finally {
            ds.stopTrace(time, sql);
        }
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return instance.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return instance.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        instance.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return instance.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        instance.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return instance.isCloseOnCompletion();
    }
}
