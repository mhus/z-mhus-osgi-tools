package de.mhus.osgi.vaadinbridge;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

public class Resource {

	private URL url;
	private Bundle bundle;

	public Resource(Bundle bundle, URL url) {
		this.bundle = bundle;
		this.url = url;
	}

	public String getMimeType(ServletContext context) throws ServletException, IOException {
		String path = getPath();
		if (path == null) return "text/plain";
		String mime = context == null ? null : context.getMimeType(getPath());
		if (mime == null) {
			path = path.toLowerCase();
			if (path.endsWith(".css")) {
				return "text/css";
			} else
			if (path.endsWith(".js")) {
				return "application/x-javascript";
			} else
			if (path.endsWith(".gif")) {
				return "image/gif";
			} else
			if (path.endsWith(".png")) {
				return "image/png";
			} else
			if (path.endsWith(".jpg") || path.endsWith(".jpeg") ) {
				return "image/jpeg";
			} else
				return "text/plain";
				
		}
		return mime;
	}

	public String getPath() {
		return url.getPath();
	}
	
	public long getLastModified() {
		return bundle.getLastModified();
	}

	protected InputStream openStream() throws IOException {
		return url.openStream();
	}

	public void writeToStream(ServletOutputStream outputStream) throws IOException {
		
		InputStream stream = openStream();
		IOUtils.copy(stream, outputStream);
		
	}
	
}
