package de.mhus.osgi.jwsclient;

import java.io.IOException;

public abstract class Connection {

	protected Target target;
	
	public Target getTarget() {
		return target;
	}

	public abstract <T> T getService(String name, Class<? extends T> ifc) throws IOException;
	
}
