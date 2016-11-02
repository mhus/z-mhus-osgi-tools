package de.mhus.osgi.sop.impl.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.pojo.MPojo;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.action.ActionApi;
import de.mhus.osgi.sop.api.action.BpmCase;
import de.mhus.osgi.sop.api.action.BpmCase.STATUS;
import de.mhus.osgi.sop.api.action.BpmDefinition;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.api.adb.ReferenceCollector;
import de.mhus.osgi.sop.api.model.DbMetadata;
import de.mhus.osgi.sop.api.rest.AbstractNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.RestResult;
import de.mhus.osgi.sop.api.rest.RestUtil;

public class ActionApiImpl extends MLog implements ActionApi {

	@SuppressWarnings("unchecked")
	@Override
	public RestResult doExecuteRestAction(CallContext callContext, String action, String source) throws MException {

		ActionApi bpm = Sop.getApi(ActionApi.class);
		
		long timeout = MTimeInterval.MINUTE_IN_MILLISECOUNDS * 10;
		
		HashMap<String, Object> parameters = new HashMap<>();
		for (String name : callContext.getParameterNames()) {
			if (!name.startsWith(AbstractNode.INTERNAL_PREFIX))
				parameters.put(name, callContext.getParameter(name));
		}

		for (String name : callContext.getNames()) {
//			if (name.endsWith(AbstractNode.ID)) {
//				parameters.put(name, String.valueOf(callContext.get(name)));
//			} else 
			if (name.endsWith(AbstractNode.OBJECT)) {
				Object val = callContext.get(name);
				if (val instanceof DbMetadata)
					parameters.put( RestUtil.getObjectIdParameterName( (Class<? extends DbMetadata>) val.getClass() ), String.valueOf( ((DbMetadata)val).getId() ) );
			}
		}
		
		parameters.put(AbstractNode.SOURCE, source );
		
		if (action == null) action = callContext.getAction();
		BpmCase res = bpm.createCase(null, action, parameters, true, timeout);
		
		DbManager manager = Sop.getApi(AdbApi.class).getManager();
		DbSchema schema = manager.getSchema();
		JsonResult result = new JsonResult();
		ObjectNode jRoot = result.createObjectNode();
		try {
			MPojo.pojoToJson(res, jRoot, schema);
		} catch (IOException e) {
			log().e(e);
		}

		return result;
	}


	@Override
	public BpmCase getCase(String id) throws MException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BpmDefinition getDefinition(String id) throws MException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BpmCase> getCases(STATUS status, int page) throws MException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BpmCase> getCases(String search) throws MException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BpmCase createCase(String customId, String process, Map<String, Object> parameters, boolean secure,
			long timeout) throws MException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BpmCase createCase(String customId, String process, Map<String, Object> parameters, String user, String pass,
			boolean secure, long timeout) throws MException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(BpmCase item) throws MException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean syncBpm(BpmCase item, boolean forced) throws MException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getUpdateInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setUpdateInterval(long updateInterval) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareSecure(BpmCase bpm, boolean created) throws MException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BpmDefinition> getDefinitions(boolean dynamic) throws MException {
		// TODO Auto-generated method stub
		return null;
	}

}
