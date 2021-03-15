package de.mhus.osgi.api.services;

import java.util.List;

import de.mhus.lib.core.operation.Operation;

public interface OperationManager {

	public Operation getOperation(String name);

	public List<Operation> getOperations();
	
}
