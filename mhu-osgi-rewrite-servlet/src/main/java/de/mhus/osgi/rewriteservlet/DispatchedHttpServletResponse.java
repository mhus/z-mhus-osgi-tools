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
package de.mhus.osgi.rewriteservlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class DispatchedHttpServletResponse implements HttpServletResponse {

	private HttpServletResponse instace;
	@Override public String getContentType() {
		return instace.getContentType();
	}

	@Override public void setCharacterEncoding(String charset) {
		instace.setCharacterEncoding(charset);
	}

	@Override public void addDateHeader(String name, long date) {
		instace.addDateHeader(name, date);
	}

	@Override public void addHeader(String name, String value) {
		instace.addHeader(name, value);
	}

	@Override public void setBufferSize(int size) {
		instace.setBufferSize(size);
	}

	@Override public void addIntHeader(String name, int value) {
		instace.addIntHeader(name, value);
	}

	@Override public int getBufferSize() {
		return instace.getBufferSize();
	}

	@Override public void flushBuffer() throws IOException {
		instace.flushBuffer();
	}

	@Override public void resetBuffer() {
		instace.resetBuffer();
	}

	@Override public boolean isCommitted() {
		return instace.isCommitted();
	}

	@Override public void reset() {
		instace.reset();
	}

	@Override public void setLocale(Locale loc) {
		instace.setLocale(loc);
	}

	@Override public Locale getLocale() {
		return instace.getLocale();
	}

	private StringWriter sw;
	private PrintWriter writer;
	private ByteArrayOutputStream os;
	private ServletOutputStream sos;

	
	public String getContent() {
		if (writer != null) {
			writer.flush();
			return sw.toString();
		}
		if (os != null) {
			try {
				sos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new String(os.toByteArray());
		}
		return null;
	}
	
	@Override public void addCookie(Cookie arg0) {
		instace.addCookie(arg0);
	}

	@Override public boolean containsHeader(String arg0) {
		return instace.containsHeader(arg0);
	}

	@Override public String encodeRedirectURL(String arg0) {
		return instace.encodeRedirectURL(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override public String encodeRedirectUrl(String arg0) {
		return instace.encodeRedirectUrl(arg0);
	}

	@Override public String encodeURL(String arg0) {
		return instace.encodeURL(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override public String encodeUrl(String arg0) {
		return instace.encodeUrl(arg0);
	}

	@Override public String getCharacterEncoding() {
		return instace.getCharacterEncoding();
	}

	@Override public ServletOutputStream getOutputStream() throws IOException {
//		return instace.getOutputStream();
		if (os == null) {
			os = new ByteArrayOutputStream();
			sos = new ServletOutputStream() {
				
				@Override
				public void write(int b) throws IOException {
					os.write(b);
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {
				}
			};
		}
		return sos;
	}

	@Override public PrintWriter getWriter() throws IOException {
//		return instace.getWriter();
		if (writer == null) {
			sw = new StringWriter();
			writer = new PrintWriter(sw);
		}
		return writer;
	}

	@Override public void sendError(int arg0, String arg1) throws IOException {
		instace.sendError(arg0, arg1);
	}

	@Override public void sendError(int arg0) throws IOException {
		instace.sendError(arg0);
	}

	@Override public void sendRedirect(String arg0) throws IOException {
		instace.sendRedirect(arg0);
	}

	@Override public void setContentLength(int arg0) {
		instace.setContentLength(arg0);
	}

	@Override public void setContentType(String arg0) {
		instace.setContentType(arg0);
	}

	@Override public void setDateHeader(String arg0, long arg1) {
		instace.setDateHeader(arg0, arg1);
	}

	@Override public void setHeader(String arg0, String arg1) {
		instace.setHeader(arg0, arg1);
	}

	@Override public void setIntHeader(String arg0, int arg1) {
		instace.setIntHeader(arg0, arg1);
	}

	@SuppressWarnings("deprecation")
	@Override public void setStatus(int arg0, String arg1) {
		instace.setStatus(arg0, arg1);
	}

	@Override public void setStatus(int arg0) {
		instace.setStatus(arg0);
	}

	public DispatchedHttpServletResponse(HttpServletResponse instance) {
		this.instace = instance;
	}

	@Override
	public void setContentLengthLong(long len) {
		instace.setContentLengthLong(len);
	}

	@Override
	public int getStatus() {
		return instace.getStatus();
	}

	@Override
	public String getHeader(String name) {
		return instace.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return instace.getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return instace.getHeaderNames();
	}
}
