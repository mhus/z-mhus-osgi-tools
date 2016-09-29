package de.mhus.osgi.jwsclient;

import java.io.IOException;

public class FactoryNotFoundException extends IOException {

	public FactoryNotFoundException(String name, String url) {
		super("Factory " + name + " not found in: " + url);
	}

	private static final long serialVersionUID = 1L;

}
