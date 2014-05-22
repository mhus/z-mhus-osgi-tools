package de.mhus.osgi.vaadinbridge.impl;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class ConfigUpdater implements ManagedService {

	private BundleWatchImpl watch;

	public ConfigUpdater(BundleWatchImpl bundleWatchImpl) {
		watch = bundleWatchImpl;
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		if (properties == null) {
		      return;
	    }	
		
		watch.setEnabled(!"false".equals(String.valueOf(properties.get("enabled"))));
		
	}

}
