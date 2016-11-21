package de.mhus.osgi.sop.api.operation;

import java.util.Collection;

import de.mhus.lib.core.strategy.Operation;
import de.mhus.osgi.sop.api.action.ActionDescriptor;

public class OperationDescriptor extends ActionDescriptor {

	public OperationDescriptor(Operation operation, Collection<String> tags, String source) {
		super(new OperationAction(operation), tags, source, operation.getDescription().getPath());
	}
	
	public Operation getOperation() {
		return getAction().adaptTo(Operation.class);
	}

}
