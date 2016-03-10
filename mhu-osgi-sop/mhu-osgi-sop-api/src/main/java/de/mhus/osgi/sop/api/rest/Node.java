package de.mhus.osgi.sop.api.rest;

import java.util.List;

public interface Node {

	String SEARCH = "_search";

	Node lookup(List<String> parts, CallContext callContext) throws Exception;

	RestResult doRead(CallContext callContext) throws Exception;

	RestResult doAction(CallContext callContext) throws Exception;

	RestResult doCreate(CallContext callContext) throws Exception;

	RestResult doUpdate(CallContext callContext) throws Exception;

	RestResult doDelete(CallContext callContext) throws Exception;

}
