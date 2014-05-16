package de.mhus.test.ws.ws_client.web;

import java.lang.reflect.Method;

public class SessionUtil {

	public static Object getAttribute(Object session, String name) throws Exception {
		Method method = session.getClass().getMethod("getAttribute", String.class);
		Object result = method.invoke(session, name);
		return result;
	}
	
	public static void setAttribute(Object session, String name, Object object) throws Exception {
		Method method = session.getClass().getMethod("setAttribute", String.class, Object.class);
		method.invoke(session, name, object);
	}
	
}
