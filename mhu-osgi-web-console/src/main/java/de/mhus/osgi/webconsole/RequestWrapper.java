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

	public Object getAttribute(String arg0) {
		return request.getAttribute(arg0);
	}

	public Enumeration getAttributeNames() {
		return request.getAttributeNames();
	}

	public String getAuthType() {
		return request.getAuthType();
	}

	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	public int getContentLength() {
		return request.getContentLength();
	}

	public String getContentType() {
		return request.getContentType();
	}

	public Cookie[] getCookies() {
		return request.getCookies();
	}

	public long getDateHeader(String arg0) {
		return request.getDateHeader(arg0);
	}

	public String getHeader(String arg0) {
		return request.getHeader(arg0);
	}

	public Enumeration getHeaderNames() {
		return request.getHeaderNames();
	}

	public ServletInputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	public int getIntHeader(String arg0) {
		return request.getIntHeader(arg0);
	}

	public String getMethod() {
		return request.getMethod();
	}

	public String getParameter(String arg0) {
		return request.getParameter(arg0);
	}

	public Enumeration getParameterNames() {
		return request.getParameterNames();
	}

	public String[] getParameterValues(String arg0) {
		return request.getParameterValues(arg0);
	}

	public String getPathInfo() {
		String out = request.getPathInfo();
		if (out == null) return "";
		return out;
	}

	public String getPathTranslated() {
		return request.getPathTranslated();
	}

	public String getProtocol() {
		return request.getProtocol();
	}

	public String getQueryString() {
		return request.getQueryString();
	}

	public BufferedReader getReader() throws IOException {
		return request.getReader();
	}

	public String getRealPath(String arg0) {
		return request.getRealPath(arg0);
	}

	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	public String getRemoteUser() {
		return request.getRemoteUser();
	}

	public String getRequestURI() {
		return request.getRequestURI();
	}

	public String getRequestedSessionId() {
		return request.getRequestedSessionId();
	}

	public String getScheme() {
		return request.getScheme();
	}

	public String getServerName() {
		return request.getServerName();
	}

	public int getServerPort() {
		return request.getServerPort();
	}

	public String getServletPath() {
		return request.getServletPath();
	}

	public HttpSession getSession() {
		return request.getSession();
	}

	public HttpSession getSession(boolean arg0) {
		return request.getSession(arg0);
	}

	public boolean isRequestedSessionIdFromCookie() {
		return request.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return request.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return request.isRequestedSessionIdFromUrl();
	}

	public boolean isRequestedSessionIdValid() {
		return request.isRequestedSessionIdValid();
	}

	public void setAttribute(String arg0, Object arg1) {
		request.setAttribute(arg0, arg1);
	}
	
}
