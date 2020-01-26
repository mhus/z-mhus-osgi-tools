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

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getDataSource().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getDataSource().isWrapperFor(iface);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new DelegateConnection(getDataSource().getConnection(), this);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
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

    @Override
    public String getDelegateURL() {
        return getInstanceName() + (!isInstanceConnected() ? " (disconnected)" : "");
    }
}
