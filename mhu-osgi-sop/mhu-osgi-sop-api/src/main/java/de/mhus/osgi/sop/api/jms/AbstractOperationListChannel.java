package de.mhus.osgi.sop.api.jms;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;

/**
 * Abstract operation execution but this one can handle a list of operations.
 * add/put and remove operations dynamically to the list or in doActivate()
 * 
 * @see AbstractJmsOperationExecuteChannel
 * @author mikehummel
 *
 */
public abstract class AbstractOperationListChannel extends AbstractJmsOperationExecuteChannel {

	private HashMap<String, Operation> operations = new HashMap<String, Operation>();
	
	@Override
	protected OperationResult doExecute(String path, IProperties properties) {
		Operation oper = getOperation(path);
		if (oper == null) return new NotSuccessful(path,"not found",OperationResult.NOT_FOUND);
		DefaultTaskContext context = new DefaultTaskContext(getClass());
		context.setParameters(properties);
		try {
			return oper.doExecute(context);
		} catch (Throwable t) {
			log().d(path,t);
			return new NotSuccessful(path, t.toString(), OperationResult.INTERNAL_ERROR);
		}
	}

	protected Operation getOperation(String path) {
		synchronized (operations) {
			return operations.get(path);
		}
	}

	@Override
	protected List<String> getPublicOperations() {
		synchronized (operations) {
			LinkedList<String> out = new LinkedList<String>(operations.keySet());
			return out;
		}
	}

	@Override
	protected OperationDescription getOperationDescription(String path) {
		Operation oper = getOperation(path);
		if (oper == null) return null;
		return oper.getDescription();
	}

	protected void add(Operation operation) {
		put(operation.getClass().getCanonicalName(),operation);
	}
	
	protected void put(String path, Operation operation) {
		synchronized (operations) {
			operations.put(path, operation);
		}
	}
	
	protected void remove(String path) {
		synchronized (operations) {
			operations.remove(path);
		}
	}
	
}
