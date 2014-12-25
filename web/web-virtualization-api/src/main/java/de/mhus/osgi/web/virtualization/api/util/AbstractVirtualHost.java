package de.mhus.osgi.web.virtualization.api.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

import de.mhus.lib.core.logging.Log;
import de.mhus.osgi.web.virtualization.api.VirtualHost;

public abstract class AbstractVirtualHost implements VirtualHost {

	protected HashMap<String, Object> attr = new HashMap<>();
	protected LinkedList<String> nativePathes = new LinkedList<>();
	protected Log log;
	protected ClassLoader classLoader = getClass().getClassLoader();
	
	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public void setAttribute(String key, Object value) {
		attr.put(key, value);
	}

	@Override
	public Object getAttribute(String key) {
		return attr.get(key);
	}

	@Override
	public boolean allowNativeRequest(String target) {
		for (String line : nativePathes)
			if (target.matches(line)) return true;
		return false;
	}

	public ClassLoader getHostClassLoader() {
		return classLoader;
	}

}
