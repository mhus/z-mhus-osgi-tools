package de.mhus.osgi.sop.api.operation;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.Monitor;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.osgi.sop.api.action.Action;

public class OperationAction implements Action {

	private Operation operation;
	private String name;

	public OperationAction(Operation operation) {
		this.operation = operation;
		this.name = operation.getDescription().getPath();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <I> I adaptTo(Class<? extends Object> ifc) {
		if ( Operation.class == ifc)
			return (I) operation;
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canExecute(IProperties properties) {
		DefaultTaskContext context = new DefaultTaskContext(operation.getClass());
		context.setParameters(properties);
		return operation.canExecute(context);
	}

	@Override
	public OperationResult doExecute(IProperties properties, Monitor monitor) throws Exception {
		DefaultTaskContext context = new DefaultTaskContext(operation.getClass());
		context.setParameters(properties);
		return operation.doExecute(context);
	}

}
