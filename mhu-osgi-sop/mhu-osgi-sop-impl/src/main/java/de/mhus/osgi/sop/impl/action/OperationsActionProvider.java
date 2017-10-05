package de.mhus.osgi.sop.impl.action;

import java.util.Collection;
import java.util.LinkedList;

import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.sop.api.action.ActionDescriptor;
import de.mhus.osgi.sop.api.action.ActionProvider;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.impl.operation.LocalOperationApiImpl;

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
		for (String name : LocalOperationApiImpl.instance.getOperations()) {
			OperationDescriptor oper = LocalOperationApiImpl.instance.getOperation(name);
			out.add(oper);
		}
		return out;
	}

	@Override
	public ActionDescriptor getAction(String name) {
		return LocalOperationApiImpl.instance.getOperation(name);
	}

}
