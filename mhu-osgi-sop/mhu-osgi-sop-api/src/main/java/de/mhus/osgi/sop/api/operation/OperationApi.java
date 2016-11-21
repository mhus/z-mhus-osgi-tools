package de.mhus.osgi.sop.api.operation;

import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.action.ActionDescriptor;

public interface OperationApi extends SApi {

	String[] getGroups();
	String[] getOperations();
	String[] getOperationForGroup(String group);
	OperationDescriptor getOperation(String path);
	
	OperationResult doExecute(String path, IProperties properties);
	
}
