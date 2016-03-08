package org.apache.commons.discovery.defaults;

import java.util.Properties;

import org.apache.commons.discovery.log.SimpleLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;

public class MyLogFactory extends org.apache.commons.logging.LogFactory {

	private Properties attr = new Properties();
	
	@Override
	public Object getAttribute(String name) {
		return attr.get(name);
	}

	@Override
	public String[] getAttributeNames() {
		return attr.keySet().toArray(new String[0]);
	}

	@Override
	public Log getInstance(Class clazz) throws LogConfigurationException {
		return new SimpleLog(clazz.getCanonicalName());
	}

	@Override
	public Log getInstance(String name) throws LogConfigurationException {
		return new SimpleLog(name);
	}

	@Override
	public void release() {
		
	}

	@Override
	public void removeAttribute(String name) {
		attr.remove(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		attr.put(name, value);
	}

}
