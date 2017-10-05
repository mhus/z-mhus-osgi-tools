package de.mhus.osgi.sop.api.operation;

import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;

public interface JmsOperationApi extends SApi {

	public static final String OPT_NEED_ANSWER = "needAnswer";
	public static final String OPT_FORCE_MAP_MESSAGE = "forceMapMessage";
	public static final String REGISTRY_TOPIC = "sop.registry";
	
	// Remote Operations API
	
	List<String> doGetOperationList(JmsConnection con, String queueName,
			AaaContext user) throws Exception;
	
	OperationResult doExecuteOperation(JmsConnection con, String queueName,
			String operationName, IProperties parameters, String ticket,
			long timeout, String ... options ) throws Exception;
	
	OperationResult doExecuteOperation(JmsConnection con, String queueName,
			String operationName, IProperties parameters, AaaContext user,
			String ... options) throws Exception;
	List<String> lookupOperationQueues() throws Exception;
	
	void sendLocalOperations();
	
	void requestOperationRegistry();
	
	List<OperationRegister> getRegisteredOperations();
	
	OperationRegister getRegisteredOperation(String path, VersionRange version);
	
}
