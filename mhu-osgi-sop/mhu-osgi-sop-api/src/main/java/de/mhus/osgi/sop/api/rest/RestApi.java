package de.mhus.osgi.sop.api.rest;

import java.util.List;
import java.util.Map;

import de.mhus.osgi.sop.api.SApi;


public interface RestApi extends SApi {

	Node lookup(List<String> parts, String lastNodeId, CallContext context)
			throws Exception;

	Map<String, RestNodeService> getRestNodeRegistry();
	
}
