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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import de.mhus.osgi.commands.db.TraceDataSource;

public class TracedConnection extends DelegateConnection {

    public TracedConnection(Connection connection, TraceDataSource dataSource) {
        super(connection, dataSource);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new TracedStatement(instance.createStatement(), this);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new TracedPreparedStatement(instance.prepareStatement(sql), sql, this);
    }
}
