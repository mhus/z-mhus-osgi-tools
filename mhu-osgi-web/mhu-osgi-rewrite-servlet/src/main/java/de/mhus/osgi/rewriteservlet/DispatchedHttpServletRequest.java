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

	public Object getAttribute(String name) {
		return instance.getAttribute(name);
	}

	public String getAuthType() {
		return instance.getAuthType();
	}

	public Cookie[] getCookies() {
		return instance.getCookies();
	}

	public Enumeration getAttributeNames() {
		return instance.getAttributeNames();
	}

	public long getDateHeader(String name) {
		return instance.getDateHeader(name);
	}

	public String getCharacterEncoding() {
		return instance.getCharacterEncoding();
	}

	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		instance.setCharacterEncoding(env);
	}

	public String getHeader(String name) {
		return instance.getHeader(name);
	}

	public int getContentLength() {
		return instance.getContentLength();
	}

	public String getContentType() {
		return instance.getContentType();
	}

	public Enumeration getHeaders(String name) {
		return instance.getHeaders(name);
	}

	public ServletInputStream getInputStream() throws IOException {
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

	public String getParameter(String name) {
		return instance.getParameter(name);
	}

	public Enumeration getHeaderNames() {
		return instance.getHeaderNames();
	}

	public int getIntHeader(String name) {
		return instance.getIntHeader(name);
	}

	public Enumeration getParameterNames() {
		return instance.getParameterNames();
	}

	public String getMethod() {
		return instance.getMethod();
	}

	public String[] getParameterValues(String name) {
		return instance.getParameterValues(name);
	}

	public String getPathInfo() {
//		return instance.getPathInfo();
		return path;
	}

	public Map getParameterMap() {
		return instance.getParameterMap();
	}

	public String getPathTranslated() {
		return instance.getPathTranslated();
	}

	public String getProtocol() {
		return instance.getProtocol();
	}

	public String getScheme() {
		return instance.getScheme();
	}

	public String getContextPath() {
		return instance.getContextPath();
	}

	public String getServerName() {
		return instance.getServerName();
	}

	public String getQueryString() {
		return instance.getQueryString();
	}

	public int getServerPort() {
		return instance.getServerPort();
	}

	public BufferedReader getReader() throws IOException {
		return instance.getReader();
	}

	public String getRemoteUser() {
		return instance.getRemoteUser();
	}

	public boolean isUserInRole(String role) {
		return instance.isUserInRole(role);
	}

	public String getRemoteAddr() {
		return instance.getRemoteAddr();
	}

	public String getRemoteHost() {
		return instance.getRemoteHost();
	}

	public Principal getUserPrincipal() {
		return instance.getUserPrincipal();
	}

	public String getRequestedSessionId() {
		return instance.getRequestedSessionId();
	}

	public void setAttribute(String name, Object o) {
		instance.setAttribute(name, o);
	}

	public String getRequestURI() {
		return instance.getRequestURI();
	}

	public void removeAttribute(String name) {
		instance.removeAttribute(name);
	}

	public Locale getLocale() {
		return instance.getLocale();
	}

	public StringBuffer getRequestURL() {
		return instance.getRequestURL();
	}

	public Enumeration getLocales() {
		return instance.getLocales();
	}

	public String getServletPath() {
		return instance.getServletPath();
	}

	public boolean isSecure() {
		return instance.isSecure();
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return instance.getRequestDispatcher(path);
	}

	public HttpSession getSession(boolean create) {
		return instance.getSession(create);
	}

	public HttpSession getSession() {
		return instance.getSession();
	}

	public String getRealPath(String path) {
		return instance.getRealPath(path);
	}

	public int getRemotePort() {
		return instance.getRemotePort();
	}

	public boolean isRequestedSessionIdValid() {
		return instance.isRequestedSessionIdValid();
	}

	public String getLocalName() {
		return instance.getLocalName();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return instance.isRequestedSessionIdFromCookie();
	}

	public String getLocalAddr() {
		return instance.getLocalAddr();
	}

	public boolean isRequestedSessionIdFromURL() {
		return instance.isRequestedSessionIdFromURL();
	}

	public int getLocalPort() {
		return instance.getLocalPort();
	}

	public boolean isRequestedSessionIdFromUrl() {
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
