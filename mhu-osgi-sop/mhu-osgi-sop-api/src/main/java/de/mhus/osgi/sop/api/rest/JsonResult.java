package de.mhus.osgi.sop.api.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.logging.LevelMapper;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.TrailLevelMapper;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;

public class JsonResult implements RestResult {

	private static Log log = Log.getLog(JsonResult.class);
	private static int nextId = 0;
	private org.codehaus.jackson.JsonNode json;
	private long id;
	private static ObjectMapper m = new ObjectMapper();
	
	public JsonResult() {
		id = newId();
	}
	
	@Override
	public void write(PrintWriter writer) throws Exception {
		
    	log.d("result",id,json);
    	if (json == null) {
    		createObjectNode();
    	}
    	if (json.isObject()) {
    		((ObjectNode)json).put("_timestamp", System.currentTimeMillis());
    		((ObjectNode)json).put("_sequence", id);

    		AaaContext user = MApi.lookup(AccessApi.class).getCurrentOrGuest();
    		((ObjectNode)json).put("_user", user.getAccountId());
    		if (user.isAdminMode())
        		((ObjectNode)json).put("_admin", true);
    			 
    		LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
    		if (lm != null && lm instanceof TrailLevelMapper && ((TrailLevelMapper)lm).isLocalTrail() )
    			((ObjectNode)json).put("_trail",((TrailLevelMapper)lm).getTrailId());
    	}
    	
		m.writeValue(writer,json);

	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	private static synchronized long newId() {
		return nextId ++;
	}

	public JsonNode getJson() {
		return json;
	}

	public void setJson(ObjectNode json) {
		this.json = json;
	}

	public ObjectNode createObjectNode() {
    	json = m.createObjectNode();
    	return (ObjectNode)json;
	}
	
	public String toString() {
		StringWriter w = new StringWriter();
		PrintWriter p = new PrintWriter(w);
		try {
			write(p);
		} catch (Exception e) {
		}
		p.flush();
		return w.toString();
	}

	public ArrayNode createArrayNode() {
		json = m.createArrayNode();
		return (ArrayNode)json;
	}
}
