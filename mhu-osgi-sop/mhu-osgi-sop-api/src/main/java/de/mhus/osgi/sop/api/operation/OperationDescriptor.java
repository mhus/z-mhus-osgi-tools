package de.mhus.osgi.sop.api.operation;

import java.util.Collection;

import de.mhus.lib.core.definition.DefRoot;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.util.MNlsProvider;
import de.mhus.lib.core.util.ParameterDefinitions;
import de.mhus.osgi.sop.api.action.ActionDescriptor;

public class OperationDescriptor extends ActionDescriptor {

	public OperationDescriptor(
			Operation operation, 
			Collection<String> tags, 
			String source, 
			ParameterDefinitions pDef, 
			DefRoot form, 
			MNlsProvider nls, 
			String title
		) {
		super(
				new OperationAction(operation), 
				tags, 
				source, 
				operation.getDescription().getPath(), 
				operation.getDescription().getVersion(),
				pDef, 
				form, 
				nls, 
				title
			);
	}
	
	public Operation getOperation() {
		return getAction().adaptTo(Operation.class);
	}

}
