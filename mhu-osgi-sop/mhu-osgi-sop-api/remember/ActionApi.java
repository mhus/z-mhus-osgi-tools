package de.mhus.osgi.sop.api.action;

import java.util.List;
import java.util.Map;

import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.action.BpmCase.STATUS;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.RestResult;

public interface ActionApi extends SApi {

	BpmCase getCase(String id) throws MException;
	
	BpmDefinition getDefinition(String id) throws MException;
	
	List<BpmCase> getCases(STATUS status, int page) throws MException;
	
	List<BpmCase> getCases(String search) throws MException;
	
	BpmCase createCase(String customId, String process, Map<String, Object> parameters, boolean secure, long timeout) throws MException;
	
	BpmCase createCase(String customId, String process, Map<String, Object> parameters, String user, String pass, boolean secure, long timeout) throws MException;

	void delete(BpmCase item) throws MException;
	
	boolean syncBpm(BpmCase item, boolean forced) throws MException;

	long getUpdateInterval();

	void setUpdateInterval(long updateInterval);

	void prepareSecure(BpmCase bpm, boolean created) throws MException;
	
	List<BpmDefinition> getDefinitions(boolean dynamic) throws MException;
	
	/**
	 * Executes/Create a BPM Case. The Method is specially for REST execution.
	 * 
	 * @param callContext The call context of the request
	 * @param action A special action or null if the default call action should be used
	 * @param source The source on which node the is executed
	 * @return The result for the REST handling
	 * @throws MException
	 */
	RestResult doExecuteRestAction(CallContext callContext, String action, String source) throws MException;

}
