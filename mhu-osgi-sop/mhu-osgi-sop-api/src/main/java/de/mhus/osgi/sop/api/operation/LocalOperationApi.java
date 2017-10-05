package de.mhus.osgi.sop.api.operation;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.osgi.sop.api.SApi;

public interface LocalOperationApi extends SApi {

	String[] getGroups();
	String[] getOperations();
	String[] getOperationForGroup(String group);
	OperationDescriptor getOperation(String path);
	
	OperationResult doExecute(String path, IProperties properties);
	
}
