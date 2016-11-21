package de.mhus.osgi.sop.impl.action;

import java.util.Collection;
import java.util.LinkedList;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.osgi.sop.api.action.Action;
import de.mhus.osgi.sop.api.action.ActionDescriptor;
import de.mhus.osgi.sop.api.action.ActionProvider;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.impl.operation.OperationApiImpl;

@Component // enabled by default
public class OperationsActionProvider implements ActionProvider {

	@Override
	public <I> I adaptTo(Class<? extends Object> ifc) {
		return null;
	}

	@Override
	public String getName() {
		return "operations";
	}

	@Override
	public Collection<ActionDescriptor> getActions() {
		LinkedList<ActionDescriptor> out = new LinkedList<>();
		for (String name : OperationApiImpl.instance.getOperations()) {
			OperationDescriptor oper = OperationApiImpl.instance.getOperation(name);
			out.add(oper);
		}
		return null;
	}

}
