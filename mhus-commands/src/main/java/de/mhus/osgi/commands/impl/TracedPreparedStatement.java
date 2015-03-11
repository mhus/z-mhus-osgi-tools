package de.mhus.osgi.commands.impl;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import de.mhus.osgi.commands.db.TraceDataSource;

public class TracedPreparedStatement implements PreparedStatement {

	private PreparedStatement instance;
	private TracedConnection con;
	private TraceDataSource ds;
	private String sql;

	public TracedPreparedStatement(PreparedStatement prepareStatement,
			String sql, TracedConnection tracedConnection) {
		instance = prepareStatement;
		con = tracedConnection;
		ds = (TraceDataSource) con.getDataSource();
		this.sql = sql;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return instance.unwrap(iface);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeQuery(sql);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public ResultSet executeQuery() throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeQuery();
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return instance.isWrapperFor(iface);
	}

	public int executeUpdate(String sql) throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeUpdate(sql);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public int executeUpdate() throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeUpdate();
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		instance.setNull(parameterIndex, sqlType);
	}

	public void close() throws SQLException {
		instance.close();
	}

	public int getMaxFieldSize() throws SQLException {
		return instance.getMaxFieldSize();
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		instance.setBoolean(parameterIndex, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		instance.setByte(parameterIndex, x);
	}

	public void setMaxFieldSize(int max) throws SQLException {
		instance.setMaxFieldSize(max);
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		instance.setShort(parameterIndex, x);
	}

	public int getMaxRows() throws SQLException {
		return instance.getMaxRows();
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		instance.setInt(parameterIndex, x);
	}

	public void setMaxRows(int max) throws SQLException {
		instance.setMaxRows(max);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		instance.setLong(parameterIndex, x);
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		instance.setEscapeProcessing(enable);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		instance.setFloat(parameterIndex, x);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		instance.setDouble(parameterIndex, x);
	}

	public int getQueryTimeout() throws SQLException {
		return instance.getQueryTimeout();
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		instance.setQueryTimeout(seconds);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		instance.setBigDecimal(parameterIndex, x);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		instance.setString(parameterIndex, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		instance.setBytes(parameterIndex, x);
	}

	public void cancel() throws SQLException {
		instance.cancel();
	}

	public SQLWarning getWarnings() throws SQLException {
		return instance.getWarnings();
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		instance.setDate(parameterIndex, x);
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		instance.setTime(parameterIndex, x);
	}

	public void clearWarnings() throws SQLException {
		instance.clearWarnings();
	}

	public void setCursorName(String name) throws SQLException {
		instance.setCursorName(name);
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		instance.setTimestamp(parameterIndex, x);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		instance.setAsciiStream(parameterIndex, x, length);
	}

	public boolean execute(String sql) throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.execute(sql);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		instance.setUnicodeStream(parameterIndex, x, length);
	}

	public ResultSet getResultSet() throws SQLException {
		return instance.getResultSet();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		instance.setBinaryStream(parameterIndex, x, length);
	}

	public int getUpdateCount() throws SQLException {
		return instance.getUpdateCount();
	}

	public boolean getMoreResults() throws SQLException {
		return instance.getMoreResults();
	}

	public void clearParameters() throws SQLException {
		instance.clearParameters();
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		instance.setObject(parameterIndex, x, targetSqlType);
	}

	public void setFetchDirection(int direction) throws SQLException {
		instance.setFetchDirection(direction);
	}

	public int getFetchDirection() throws SQLException {
		return instance.getFetchDirection();
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		instance.setObject(parameterIndex, x);
	}

	public void setFetchSize(int rows) throws SQLException {
		instance.setFetchSize(rows);
	}

	public int getFetchSize() throws SQLException {
		return instance.getFetchSize();
	}

	public int getResultSetConcurrency() throws SQLException {
		return instance.getResultSetConcurrency();
	}

	public boolean execute() throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.execute();
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public int getResultSetType() throws SQLException {
		return instance.getResultSetType();
	}

	public void addBatch(String sql) throws SQLException {
		instance.addBatch(sql);
	}

	public void clearBatch() throws SQLException {
		instance.clearBatch();
	}

	public void addBatch() throws SQLException {
		instance.addBatch();
	}

	public int[] executeBatch() throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeBatch();
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		instance.setCharacterStream(parameterIndex, reader, length);
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		instance.setRef(parameterIndex, x);
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		instance.setBlob(parameterIndex, x);
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		instance.setClob(parameterIndex, x);
	}

	public Connection getConnection() throws SQLException {
		return instance.getConnection();
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		instance.setArray(parameterIndex, x);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return instance.getMetaData();
	}

	public boolean getMoreResults(int current) throws SQLException {
		return instance.getMoreResults(current);
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		instance.setDate(parameterIndex, x, cal);
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		return instance.getGeneratedKeys();
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		instance.setTime(parameterIndex, x, cal);
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeUpdate(sql, autoGeneratedKeys);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		instance.setTimestamp(parameterIndex, x, cal);
	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		instance.setNull(parameterIndex, sqlType, typeName);
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeUpdate(sql, columnIndexes);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		instance.setURL(parameterIndex, x);
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.executeUpdate(sql, columnNames);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		return instance.getParameterMetaData();
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		instance.setRowId(parameterIndex, x);
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		instance.setNString(parameterIndex, value);
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.execute(sql, autoGeneratedKeys);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		instance.setNCharacterStream(parameterIndex, value, length);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		instance.setNClob(parameterIndex, value);
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		instance.setClob(parameterIndex, reader, length);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.execute(sql, columnIndexes);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		instance.setBlob(parameterIndex, inputStream, length);
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		instance.setNClob(parameterIndex, reader, length);
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		long time = ds.startTrace(sql);
		try {
			return instance.execute(sql, columnNames);
		} finally {
			ds.stopTrace(time,sql);
		}
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		instance.setSQLXML(parameterIndex, xmlObject);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		instance.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
	}

	public int getResultSetHoldability() throws SQLException {
		return instance.getResultSetHoldability();
	}

	public boolean isClosed() throws SQLException {
		return instance.isClosed();
	}

	public void setPoolable(boolean poolable) throws SQLException {
		instance.setPoolable(poolable);
	}

	public boolean isPoolable() throws SQLException {
		return instance.isPoolable();
	}

	public void closeOnCompletion() throws SQLException {
		instance.closeOnCompletion();
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		instance.setAsciiStream(parameterIndex, x, length);
	}

	public boolean isCloseOnCompletion() throws SQLException {
		return instance.isCloseOnCompletion();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		instance.setBinaryStream(parameterIndex, x, length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		instance.setCharacterStream(parameterIndex, reader, length);
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		instance.setAsciiStream(parameterIndex, x);
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		instance.setBinaryStream(parameterIndex, x);
	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		instance.setCharacterStream(parameterIndex, reader);
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		instance.setNCharacterStream(parameterIndex, value);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		instance.setClob(parameterIndex, reader);
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		instance.setBlob(parameterIndex, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		instance.setNClob(parameterIndex, reader);
	}

}
