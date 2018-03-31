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
package de.mhus.osgi.webconsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RequestWrapper implements HttpServletRequest {

	private HttpServletRequest request;

	public RequestWrapper(HttpServletRequest request) {
		this.request = request;
	}

	@Override public Object getAttribute(String arg0) {
		return request.getAttribute(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override public Enumeration getAttributeNames() {
		return request.getAttributeNames();
	}

	@Override public String getAuthType() {
		return request.getAuthType();
	}

	@Override public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	@Override public int getContentLength() {
		return request.getContentLength();
	}

	@Override public String getContentType() {
		return request.getContentType();
	}

	@Override public Cookie[] getCookies() {
		return request.getCookies();
	}

	@Override public long getDateHeader(String arg0) {
		return request.getDateHeader(arg0);
	}

	@Override public String getHeader(String arg0) {
		return request.getHeader(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override public Enumeration getHeaderNames() {
		return request.getHeaderNames();
	}

	@Override public ServletInputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	@Override public int getIntHeader(String arg0) {
		return request.getIntHeader(arg0);
	}

	@Override public String getMethod() {
		return request.getMethod();
	}

	@Override public String getParameter(String arg0) {
		return request.getParameter(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override public Enumeration getParameterNames() {
		return request.getParameterNames();
	}

	@Override public String[] getParameterValues(String arg0) {
		return request.getParameterValues(arg0);
	}

	@Override public String getPathInfo() {
		String out = request.getPathInfo();
		if (out == null) return "";
		return out;
	}

	@Override public String getPathTranslated() {
		return request.getPathTranslated();
	}

	@Override public String getProtocol() {
		return request.getProtocol();
	}

	@Override public String getQueryString() {
		return request.getQueryString();
	}

	@Override public BufferedReader getReader() throws IOException {
		return request.getReader();
	}

	@SuppressWarnings("deprecation")
	@Override public String getRealPath(String arg0) {
		return request.getRealPath(arg0);
	}

	@Override public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	@Override public String getRemoteHost() {
		return request.getRemoteHost();
	}

	@Override public String getRemoteUser() {
		return request.getRemoteUser();
	}

	@Override public String getRequestURI() {
		return request.getRequestURI();
	}

	@Override public String getRequestedSessionId() {
		return request.getRequestedSessionId();
	}

	@Override public String getScheme() {
		return request.getScheme();
	}

	@Override public String getServerName() {
		return request.getServerName();
	}

	@Override public int getServerPort() {
		return request.getServerPort();
	}

	@Override public String getServletPath() {
		return request.getServletPath();
	}

	@Override public HttpSession getSession() {
		return request.getSession();
	}

	@Override public HttpSession getSession(boolean arg0) {
		return request.getSession(arg0);
	}

	@Override public boolean isRequestedSessionIdFromCookie() {
		return request.isRequestedSessionIdFromCookie();
	}

	@Override public boolean isRequestedSessionIdFromURL() {
		return request.isRequestedSessionIdFromURL();
	}

	@SuppressWarnings("deprecation")
	@Override public boolean isRequestedSessionIdFromUrl() {
		return request.isRequestedSessionIdFromUrl();
	}

	@Override public boolean isRequestedSessionIdValid() {
		return request.isRequestedSessionIdValid();
	}

	@Override public void setAttribute(String arg0, Object arg1) {
		request.setAttribute(arg0, arg1);
	}
	
}
