package de.mhus.osgi.sop.api.rest;

import java.util.List;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.adb.AdbApi;

public abstract class AbstractObjectListNode<T> extends JsonNode<T> {

	@Override
	public void doRead(JsonResult result, CallContext callContext)
			throws Exception {

		PojoModelFactory schema = getPojoModelFactory();
		
		T obj = getObjectFromContext(callContext, getManagedClass());
		if (obj != null) {
			doPrepareForOutput(obj, callContext, false);
			ObjectNode jRoot = result.createObjectNode();
			MPojo.pojoToJson(obj, jRoot, schema);
		} else {
			ArrayNode jList = result.createArrayNode();
			
			for (T item : getObjectList(callContext) ) {
				doPrepareForOutput(item, callContext, true);
				ObjectNode jItem = jList.objectNode();
				jList.add(jItem);
				MPojo.pojoToJson(item, jItem, schema);
			}
			
		}
		
	}

	protected PojoModelFactory getPojoModelFactory() {
		DbManager manager = MApi.lookup(AdbApi.class).getManager();
		DbSchema schema = manager.getSchema();
		return schema;
//		return MPojo.getDefaultModelFactory();
	}

	protected abstract List<T> getObjectList(CallContext callContext) throws MException;

	protected void doPrepareForOutput(T obj, CallContext context, boolean listMode) throws MException {
	}

	@Override
	public void doUpdate(JsonResult result, CallContext callContext)
			throws Exception {
		T obj = getObjectFromContext(callContext);
		if (obj == null) throw new RestException(OperationResult.NOT_FOUND);
		
		RestUtil.updateObject(callContext, obj, true);
	}

}
