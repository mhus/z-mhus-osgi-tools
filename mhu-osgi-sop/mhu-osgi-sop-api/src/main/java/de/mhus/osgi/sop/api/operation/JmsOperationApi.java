package de.mhus.osgi.sop.api.operation;

import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.action.ActionDescriptor;

public interface JmsOperationApi extends SApi {

	// Remote Operations API
	
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
