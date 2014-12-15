package org.apache.commons.discovery.defaults;

import de.mhus.lib.core.lang.MObject;

public class Defaults extends MObject {

	private static Defaults instance;
	
	public static synchronized Defaults instance() {
		if (instance == null)
			instance = new Defaults();
		return instance;
	}
	
	public Class<?> findClass(String name) {

		try {
			Class<?> ifc = Class.forName(name);
			Object obj = base(ifc);
			if (obj != null) return obj.getClass();
		} catch (Throwable e) {
		}
		// build in defaults
        if (name.equals("org.apache.commons.logging.LogFactory")) {
        	return MyLogFactory.class;
        }
        if (name.equals("org.apache.axis.components.net.TransportClientProperties")) {
        	return null;
        }
        System.out.println("Unknown Class: " + name);
		return null;
	}

}
