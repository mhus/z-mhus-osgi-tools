package de.mhus.osgi.commands.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class DataSourceUtil {
	
	public static final String SERVICE_JNDI_NAME_KEY = "osgi.jndi.service.name";
	private BundleContext context;
	
	public DataSourceUtil(BundleContext context) {
		this.context = context;
	}

	public DataSource getDataSource(String name) {
        ServiceReference<?>[] dataSources = getDataSources();
        for (ServiceReference<?> ref : dataSources) {
            String jndiName = getServiceJndiName(ref);
            if (name.equals(jndiName)) {
                DataSource ds = (DataSource)getContext().getService(ref);
                return ds;
            }
        }
        // throw new RuntimeException("No DataSource with name " + name + " found");
        return null;
    }

	public ServiceReference<?>[] getDataSources() {
        try {
            ServiceReference<?>[] dsRefs = getContext().getServiceReferences(DataSource.class.getName(), null);
            if (dsRefs == null) {
                dsRefs = new ServiceReference[]{};
            }
            return dsRefs;
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
        context.registerService(DataSource.class, dataSource,properties);
	}
}
