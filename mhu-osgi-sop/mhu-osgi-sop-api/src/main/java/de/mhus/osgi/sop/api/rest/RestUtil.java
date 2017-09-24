package de.mhus.osgi.sop.api.rest;

import java.io.IOException;
import java.util.UUID;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.annotations.generic.Public;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.action.ActionApi;
import de.mhus.osgi.sop.api.action.ActionDescriptor;
import de.mhus.osgi.sop.api.adb.AdbApi;

public class RestUtil {
	
	private static Log log = Log.getLog(RestUtil.class);

	public static void updateObject(CallContext callContext, Object obj, boolean publicOnly) throws IOException {
		DbManager manager = MApi.lookup(AdbApi.class).getManager();
		DbSchema schema = manager.getSchema();

		PojoModel model = schema.createPojoModel(obj.getClass());
		for (String name : callContext.getParameterNames()) {
			@SuppressWarnings("unchecked")
			PojoAttribute<Object> attr = model.getAttribute(name);
			if (attr != null) {
				Public p = attr.getAnnotation(Public.class);
				if (!publicOnly || p != null && p.readable() && p.writeable()) {
					// set
					attr.set(obj, callContext.getParameter(name));
				}
			}
		}
	}

	public static void updateObject(IProperties props, Object obj, boolean publicOnly) throws IOException {
		DbManager manager = MApi.lookup(AdbApi.class).getManager();
		DbSchema schema = manager.getSchema();

		PojoModel model = schema.createPojoModel(obj.getClass());
		for (String name : props.keys()) {
			@SuppressWarnings("unchecked")
			PojoAttribute<Object> attr = model.getAttribute(name);
			if (attr != null) {
				Public p = attr.getAnnotation(Public.class);
				if (!publicOnly || p != null && p.readable() && p.writeable()) {
					// set
					attr.set(obj, props.get(name));
				}
			}
		}
	}

	public static String getObjectIdParameterName(Class<? extends DbMetadata> clazz) {
		return clazz.getSimpleName().toLowerCase() + "Id";
	}

	public static UUID getObjectUuid(CallContext callContext, Class<? extends DbMetadata> clazz) {
		return UUID.fromString( callContext.getParameter(getObjectIdParameterName(clazz)) );
	}
	
	public static String getObjectId(CallContext callContext, Class<? extends DbMetadata> clazz) {
		return callContext.getParameter(getObjectIdParameterName(clazz));
	}

	public static RestResult doExecuteBpm(ActionDescriptor oper, CallContext callContext, String source) throws MException {
		return doExecuteRestAction(callContext, oper, source );
	}
	
	public static RestResult doExecuteBpm(String name, CallContext callContext, String source) throws MException {
		return doExecuteRestAction(callContext, MApi.lookup(ActionApi.class).getAction(name), source );
	}

	private static RestResult doExecuteRestAction(CallContext callContext, ActionDescriptor descriptor, String source) {
		// TODO Auto-generated method stub
		return null;
	}

}
