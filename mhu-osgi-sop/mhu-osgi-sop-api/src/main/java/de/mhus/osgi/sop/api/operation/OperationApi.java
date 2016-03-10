package de.mhus.osgi.sop.api.operation;

import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;

public interface OperationApi extends SApi {

	String[] getGroups();
	String[] getOperations();
	String[] getOperationForGroup(String group);
	Operation getOperation(String path);
	
	OperationResult doExecute(String path, IProperties properties);
	
	OperationBpmDefinition getActionDefinition(String prozess);
	List<OperationBpmDefinition> getActionDefinitions();
	List<String> doGetOperationList(JmsConnection con, String queueName,
			AaaContext user) throws Exception;
	OperationResult doExecuteOperation(JmsConnection con, String queueName,
			String operationName, IProperties parameters, String ticket,
			long timeout, boolean needAnswer) throws Exception;
	OperationResult doExecuteOperation(JmsConnection con, String queueName,
			String operationName, IProperties parameters, AaaContext user,
			boolean needAnswer) throws Exception;
	List<String> lookupOperationQueues() throws Exception;
}
