package de.mhus.osgi.jwsclient;

import java.io.IOException;

import de.mhus.osgi.jwsclient.impl.ServcieNotFoundException;

public abstract class Connection {

	protected Target target;
	
	public Target getTarget() {
		return target;
	}

	public <T> T getService(Class<? extends T> ifc) throws IOException {
		for (String name :getServiceNames()) {
			if (name.startsWith(ifc.getSimpleName()))
				return getService(name, ifc);
		}
		throw new ServcieNotFoundException(ifc.getSimpleName());
	}
	
	public abstract <T> T getService(String name, Class<? extends T> ifc) throws IOException;
	
	public abstract String[] getServiceNames();
	
}
