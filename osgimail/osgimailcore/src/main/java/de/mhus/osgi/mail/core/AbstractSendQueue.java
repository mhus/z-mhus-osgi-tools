package de.mhus.osgi.mail.core;

import java.util.Properties;

public abstract class AbstractSendQueue implements SendQueue {

	protected String name = getClass().getSimpleName();
	protected Properties properties = new Properties();

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Properties getProperties() {
		return properties;
	}
	
	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
		doPropertiesUpdated();
	}

	/**
	 * The method is called after a update of the properties.
	 */
	protected abstract void doPropertiesUpdated();
	
}
