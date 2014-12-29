package de.mhus.osgi.web.virtualization.api;

import de.mhus.lib.core.directory.ResourceNode;

public interface MimeTypeFinder {

	String getMimeType(ResourceNode res);
	
	String getMimeType(String res);
	
}
