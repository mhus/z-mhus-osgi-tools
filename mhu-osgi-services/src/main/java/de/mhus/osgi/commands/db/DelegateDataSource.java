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
package de.mhus.osgi.commands.db;

import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import de.mhus.osgi.api.util.DataSourceUtil;
import de.mhus.osgi.commands.impl.AbstractDataSource;

public class DelegateDataSource extends AbstractDataSource {

    private String source;
    private BundleContext context;
    private DataSource dataSource;

    @Override
    public DataSource getDataSource() throws SQLFeatureNotSupportedException {

        synchronized (this) {
            if (context == null)
                context = FrameworkUtil.getBundle(DataSource.class).getBundleContext();

            if (dataSource == null) {
                dataSource = new DataSourceUtil(context).getDataSource(source);
            }
        }

        return dataSource;
    }

    @Override
    public void doDisconnect() {
        dataSource = null;
    }

    @Override
    public boolean isInstanceConnected() {
        return dataSource != null;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
        instanceName = "delegate:" + source;
    }

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }
}
