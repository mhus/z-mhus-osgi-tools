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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class DispatchedHttpServletRequest implements HttpServletRequest {

	private String path;
	private HttpServletRequest instance;
	private byte[] inputBytes;

	@Override public Object getAttribute(String name) {
		return instance.getAttribute(name);
	}

	@Override public String getAuthType() {
		return instance.getAuthType();
	}

	@Override public Cookie[] getCookies() {
		return instance.getCookies();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override public Enumeration getAttributeNames() {
		return instance.getAttributeNames();
	}

	@Override public long getDateHeader(String name) {
		return instance.getDateHeader(name);
	}

	@Override public String getCharacterEncoding() {
		return instance.getCharacterEncoding();
	}

	@Override public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		instance.setCharacterEncoding(env);
	}

	@Override public String getHeader(String name) {
		return instance.getHeader(name);
	}

	@Override public int getContentLength() {
		return instance.getContentLength();
	}

	@Override public String getContentType() {
		return instance.getContentType();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public Enumeration getHeaders(String name) {
		return instance.getHeaders(name);
	}

	@Override public ServletInputStream getInputStream() throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ServletInputStream is = instance.getInputStream();
		while(true) {
			int b = is.read();
			if (b < 0) break;
			bs.write(b);
		}
		inputBytes = bs.toByteArray();
		
		return new MyServletInputStream();
	}

	@Override public String getParameter(String name) {
		return instance.getParameter(name);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public Enumeration getHeaderNames() {
		return instance.getHeaderNames();
	}

	@Override public int getIntHeader(String name) {
		return instance.getIntHeader(name);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public Enumeration getParameterNames() {
		return instance.getParameterNames();
	}

	@Override public String getMethod() {
		return instance.getMethod();
	}

	@Override public String[] getParameterValues(String name) {
		return instance.getParameterValues(name);
	}

	@Override public String getPathInfo() {
//		return instance.getPathInfo();
		return path;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public Map getParameterMap() {
		return instance.getParameterMap();
	}

	@Override public String getPathTranslated() {
		return instance.getPathTranslated();
	}

	@Override public String getProtocol() {
		return instance.getProtocol();
	}

	@Override public String getScheme() {
		return instance.getScheme();
	}

	@Override public String getContextPath() {
		return instance.getContextPath();
	}

	@Override public String getServerName() {
		return instance.getServerName();
	}

	@Override public String getQueryString() {
		return instance.getQueryString();
	}

	@Override public int getServerPort() {
		return instance.getServerPort();
	}

	@Override public BufferedReader getReader() throws IOException {
		return instance.getReader();
	}

	@Override public String getRemoteUser() {
		return instance.getRemoteUser();
	}

	@Override public boolean isUserInRole(String role) {
		return instance.isUserInRole(role);
	}

	@Override public String getRemoteAddr() {
		return instance.getRemoteAddr();
	}

	@Override public String getRemoteHost() {
		return instance.getRemoteHost();
	}

	@Override public Principal getUserPrincipal() {
		return instance.getUserPrincipal();
	}

	@Override public String getRequestedSessionId() {
		return instance.getRequestedSessionId();
	}

	@Override public void setAttribute(String name, Object o) {
		instance.setAttribute(name, o);
	}

	@Override public String getRequestURI() {
		return instance.getRequestURI();
	}

	@Override public void removeAttribute(String name) {
		instance.removeAttribute(name);
	}

	@Override public Locale getLocale() {
		return instance.getLocale();
	}

	@Override public StringBuffer getRequestURL() {
		return instance.getRequestURL();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override public Enumeration getLocales() {
		return instance.getLocales();
	}

	@Override public String getServletPath() {
		return instance.getServletPath();
	}

	@Override public boolean isSecure() {
		return instance.isSecure();
	}

	@Override public RequestDispatcher getRequestDispatcher(String path) {
		return instance.getRequestDispatcher(path);
	}

	@Override public HttpSession getSession(boolean create) {
		return instance.getSession(create);
	}

	@Override public HttpSession getSession() {
		return instance.getSession();
	}

	@SuppressWarnings("deprecation")
	@Override public String getRealPath(String path) {
		return instance.getRealPath(path);
	}

	@Override public int getRemotePort() {
		return instance.getRemotePort();
	}

	@Override public boolean isRequestedSessionIdValid() {
		return instance.isRequestedSessionIdValid();
	}

	@Override public String getLocalName() {
		return instance.getLocalName();
	}

	@Override public boolean isRequestedSessionIdFromCookie() {
		return instance.isRequestedSessionIdFromCookie();
	}

	@Override public String getLocalAddr() {
		return instance.getLocalAddr();
	}

	@Override public boolean isRequestedSessionIdFromURL() {
		return instance.isRequestedSessionIdFromURL();
	}

	@Override public int getLocalPort() {
		return instance.getLocalPort();
	}

	@SuppressWarnings("deprecation")
	@Override public boolean isRequestedSessionIdFromUrl() {
		return instance.isRequestedSessionIdFromUrl();
	}

	public DispatchedHttpServletRequest(String path, HttpServletRequest req) {
		//if (path.equals("")) path = null;
		this.path = path;
		this.instance = req;
	}
	
	public byte[] getInputBytes() {
		return inputBytes;
	}

	private class MyServletInputStream extends ServletInputStream {

		private int cnt = 0;

		@Override
		public int read() throws IOException {
			if (inputBytes == null || cnt >= inputBytes.length) return -1;
			cnt++;
			return inputBytes[cnt-1];
		}

		@Override
		public boolean isFinished() {
			return inputBytes == null || cnt >= inputBytes.length;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			
		}
		
	}

	@Override
	public long getContentLengthLong() {
		return instance.getContentLengthLong();
	}

	@Override
	public ServletContext getServletContext() {
		return instance.getServletContext();
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return instance.startAsync();
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		return instance.startAsync(servletRequest, servletResponse);
	}

	@Override
	public boolean isAsyncStarted() {
		return instance.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		return instance.isAsyncSupported();
	}

	@Override
	public AsyncContext getAsyncContext() {
		return instance.getAsyncContext();
	}

	@Override
	public DispatcherType getDispatcherType() {
		return instance.getDispatcherType();
	}

	@Override
	public String changeSessionId() {
		return instance.changeSessionId();
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return instance.authenticate(response);
	}

	@Override
	public void login(String username, String password) throws ServletException {
		instance.login(username, password);		
	}

	@Override
	public void logout() throws ServletException {
		instance.logout();		
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return instance.getParts();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return instance.getPart(name);
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return instance.upgrade(handlerClass);
	}
}
