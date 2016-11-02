package de.mhus.osgi.sop.api.rest;

import java.io.IOException;
import java.util.UUID;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.annotations.generic.Public;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.action.ActionApi;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.model.DbMetadata;
import de.mhus.osgi.sop.api.operation.OperationBpmDefinition;
import de.mhus.osgi.sop.api.operation.OperationService;

public class RestUtil {
	
	private static Log log = Log.getLog(RestUtil.class);

	public static void updateObject(CallContext callContext, Object obj, boolean publicOnly) throws IOException {
		DbManager manager = Sop.getApi(AdbApi.class).getManager();
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
		DbManager manager = Sop.getApi(AdbApi.class).getManager();
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

	public static RestResult doExecuteBpm(Class<? extends OperationService> oper, CallContext callContext, String source) throws MException {
		return Sop.getApi(ActionApi.class).doExecuteRestAction(callContext, getOperationName(oper), source );
	}
	
	public static RestResult doExecuteBpm(String name, CallContext callContext, String source) throws MException {
		return Sop.getApi(ActionApi.class).doExecuteRestAction(callContext, name, source );
	}

	public static String getOperationName(Class<? extends OperationService> oper) throws MException {
		try {
			OperationBpmDefinition def = oper.newInstance().getBpmDefinition();
			return def.getName();
		} catch (InstantiationException | IllegalAccessException e) {
			log.e(oper,e);
			throw new MException(e);
		}
	}

}
