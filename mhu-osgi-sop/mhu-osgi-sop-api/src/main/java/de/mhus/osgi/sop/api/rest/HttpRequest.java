package de.mhus.osgi.sop.api.rest;

import java.util.Map;
import java.util.Set;


public class HttpRequest {

	private Map<String, String[]> parameters;

	public HttpRequest(Map<String,String[]> parameters) {
		this.parameters =parameters;
	}
	
//	public HttpRequest(Map<String,Object> parameters) {
//		this.parameters =parameters;
//	}

	public String getParameter(String key) {
		Object out = parameters.get(key);
		if (out == null) return null;
		if (out instanceof String[]) {
			String[] outArray = (String[])out;
			if (outArray.length > 0) return outArray[0];
			return null;
		}
		return String.valueOf(out);
	}

	public Set<String> getParameterNames() {
		return parameters.keySet();
	}

}
