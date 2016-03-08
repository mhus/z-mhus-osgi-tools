package de.mhus.osgi.jwsclient.impl;

import java.io.IOException;

public class ServcieNotFoundException extends IOException {

	public ServcieNotFoundException(String name) {
		super("Service " + name + " not found.");
	}

	private static final long serialVersionUID = 1L;
	

}
