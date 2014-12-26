package de.mhus.osgi.web.virtualization.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.lib.errors.MException;

public class MimeTypeFinder {

	private Properties types;
	private String defaultMime;

	public MimeTypeFinder(DefaultVirtualHost virtualHost) {
		
		types = new Properties();
		defaultMime = "text/plain";
		try {
			InputStream is = getClass().getResource("/mime.properties").openStream();
			types.load(is);
			
			File f = new File( virtualHost.getConfigRoot(), "mime.properties");
			if (f.exists()) {
				is = new FileInputStream(f);
				types.load(is);
				is.close();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public String getMimeType(ResourceNode res) {
		String ext = "";
		try {
			ext = MString.afterLastIndex(res.getName(), '.');
		} catch (MException e) {
		}
		String type = types.getProperty(ext, defaultMime);
		return type;
	}

	public String getMimeType(String res) {
		String ext = "";
		ext = MString.afterLastIndex(res, '.');
		String type = types.getProperty(ext, defaultMime);
		return type;
	}
	
}
