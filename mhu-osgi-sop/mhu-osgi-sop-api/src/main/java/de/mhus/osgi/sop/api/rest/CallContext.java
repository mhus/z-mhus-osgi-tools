package de.mhus.osgi.sop.api.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.mhus.lib.core.MApi;

public class CallContext {

	public static final String ACTION_PARAMETER = "_action";

	private HttpRequest req;
	private String method;
	private HashMap<String, Object> context;
	
	public CallContext(HttpRequest req,
			String method, HashMap<String, Object> context) {
		this.req = req;
		this.method = method;
		this.context = context;
	}

	public boolean hasAction() {
		return req.getParameter(ACTION_PARAMETER) != null;
	}
	
	public String getAction() {
		return getParameter(ACTION_PARAMETER);
	}
	
	public String getParameter(String key) {
		String val = req.getParameter(key);
		return val;
	}

	public Map<String,String> getParameters() {
		HashMap<String,String> out = new HashMap<>();
		for (String n : getParameterNames())
			out.put(n, getParameter(n));
		return out;
	}
	
	public Object get(String key) {
		return context.get(key);
	}

	public HttpRequest getRequest() {
		return req;
	}

	public void put(String key, Object value) {
		context.put(key, value);
	}
	
	public String[] getNames() {
		return context.keySet().toArray(new String[0]);
	}

	public Set<String> getParameterNames() {
		return req.getParameterNames();
	}

	public String getMethod() {
		return method;
	}

	public Node lookup(List<String> parts, String lastNodeId)
			throws Exception {
        RestApi restService = MApi.lookup(RestApi.class);
        return restService.lookup(parts, lastNodeId, this);
	}

}
