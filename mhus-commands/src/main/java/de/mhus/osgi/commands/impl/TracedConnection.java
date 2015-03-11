package de.mhus.osgi.commands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import de.mhus.osgi.commands.db.TraceDataSource;

public class TracedConnection extends DelegateConnection {

	public TracedConnection(Connection connection,
			TraceDataSource dataSource) {
		super(connection, dataSource);
	}

	public Statement createStatement() throws SQLException {
		return new TracedStatement(instance.createStatement(),this);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new TracedPreparedStatement(instance.prepareStatement(sql), sql, this);
	}

}
