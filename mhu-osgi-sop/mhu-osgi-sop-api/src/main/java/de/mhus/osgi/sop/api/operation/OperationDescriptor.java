package de.mhus.osgi.sop.api.operation;

import java.util.Collection;

import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.util.ParameterDefinitions;
import de.mhus.osgi.sop.api.action.ActionDescriptor;

public class OperationDescriptor extends ActionDescriptor {

	public OperationDescriptor(Operation operation, Collection<String> tags, String source, ParameterDefinitions pDef, String form) {
		super(new OperationAction(operation), tags, source, operation.getDescription().getPath(), pDef, form);
	}
	
	public Operation getOperation() {
		return getAction().adaptTo(Operation.class);
	}

}
