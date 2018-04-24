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
package de.mhus.lib.karaf;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;

public class DataSourceUtil {
	
	public static final String SERVICE_JNDI_NAME_KEY = "osgi.jndi.service.name";
	private BundleContext context;
	
	public DataSourceUtil() {
		this.context = FrameworkUtil.getBundle(DataSourceUtil.class).getBundleContext();
	}
	
	public DataSourceUtil(BundleContext context) {
		this.context = context;
	}

	public DataSource getDataSource(String name) {
		try {
			Collection<ServiceReference<DataSource>> refs = getContext().getServiceReferences(DataSource.class, "(" + SERVICE_JNDI_NAME_KEY + "=" + name + ")");
			if (refs.size() > 0) {
				return getContext().getService(refs.iterator().next());
			}
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}
		
//        ServiceReference<?>[] dataSources = getDataSources();
//        for (ServiceReference<?> ref : dataSources) {
//            String jndiName = getServiceJndiName(ref);
//            if (name.equals(jndiName)) {
//                DataSource ds = (DataSource)getContext().getService(ref);
//                return ds;
//            }
//        }

        // throw new RuntimeException("No DataSource with name " + name + " found");
        return null;
    }

	@SuppressWarnings("unchecked")
	public ServiceReference<DataSource>[] getDataSources() {
        try {
            ServiceReference<?>[] dsRefs = getContext().getServiceReferences(DataSource.class.getName(), null);
            if (dsRefs == null) {
                dsRefs = new ServiceReference[]{};
            }
            return (ServiceReference<DataSource>[]) dsRefs;
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
	
	public String getServiceJndiName(ServiceReference<?> ref) {
        String jndiName = (String)ref.getProperty(SERVICE_JNDI_NAME_KEY);
        return jndiName;
    }
	
	public BundleContext getContext() {
		return context;
	}

	public void registerDataSource(DataSource dataSource, String name) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(SERVICE_JNDI_NAME_KEY, name);
        properties.put(DataSourceFactory.JDBC_DATASOURCE_NAME, name);
        
        context.registerService(DataSource.class, dataSource,properties);
	}
}
