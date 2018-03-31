/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.vaadinbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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
		if (url == null) return null;
		return url.getPath();
	}
	
	public long getLastModified() {
		return bundle.getLastModified();
	}

	protected InputStream openStream() throws IOException {
		return url.openStream();
	}

	public void writeToStream(OutputStream outputStream) throws IOException {
		
		InputStream stream = openStream();
		IOUtils.copy(stream, outputStream);
		
	}

	public Bundle getBundle() {
		return bundle;
	}

	public URL getUrl() {
		return url;
	}
	
}
