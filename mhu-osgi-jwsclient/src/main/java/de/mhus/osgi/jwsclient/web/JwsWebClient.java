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
package de.mhus.osgi.jwsclient.web;

import java.io.IOException;
import java.lang.reflect.Method;

import de.mhus.osgi.jwsclient.Client;
import de.mhus.osgi.jwsclient.Connection;
import de.mhus.osgi.jwsclient.Target;
import de.mhus.osgi.jwsclient.TargetFactory;

public class JwsWebClient extends Client {

	public static final String SESSION_CONNECTION_ATTRIBUTE = "__jws_connection_";
	private static JwsWebClient instance;

	private JwsWebClient() {}
	
	public static synchronized JwsWebClient instance() {
		if (instance == null)
			instance = new JwsWebClient();
		return instance;
	}
	
	@Override
	public Target createTarget(String url) throws IOException {
		return super.createTarget(url);
	}
	
	@Override
	public void registerFactory(String name, TargetFactory factory) {
		super.registerFactory(name, factory);
	}
	
	public Connection getConnection(Object session, String target) throws Exception {
		
		if (session == null)
			return getTarget(target).createConnection();
		
		synchronized (session) {
			Connection con = (Connection) getSessionAttribute(session, SESSION_CONNECTION_ATTRIBUTE + target);
			if (con == null) {
				Target t = getTarget(target);
				con = t.createConnection();
				setSessionAttribute(session, SESSION_CONNECTION_ATTRIBUTE + target, con);
			}
			return con;
		}
		
	}

	public void closeConnection(Object session, String target) throws Exception {
		if (session == null) return;
		setSessionAttribute(session, SESSION_CONNECTION_ATTRIBUTE + target, null);
	}

	public static Object getSessionAttribute(Object session, String name) throws Exception {
		Method method = session.getClass().getMethod("getAttribute", String.class);
		Object result = method.invoke(session, name);
		return result;
	}
	
	public static void setSessionAttribute(Object session, String name, Object object) throws Exception {
		Method method = session.getClass().getMethod("setAttribute", String.class, Object.class);
		method.invoke(session, name, object);
	}

}
