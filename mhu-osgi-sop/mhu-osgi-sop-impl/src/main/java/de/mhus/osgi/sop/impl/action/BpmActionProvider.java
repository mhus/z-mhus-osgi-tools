package de.mhus.osgi.sop.impl.action;

import java.util.Collection;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.strategy.Monitor;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.osgi.sop.api.action.Action;
import de.mhus.osgi.sop.api.action.ActionDescriptor;
import de.mhus.osgi.sop.api.action.ActionProvider;

public class BpmActionProvider implements ActionProvider {

	@Override
	public <I> I adaptTo(Class<? extends Object> ifc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "bpm";
	}

	@Override
	public Collection<ActionDescriptor> getActions() {
		return null;
	}

	
	private static class BpmAction implements Action {

		@Override
		public <I> I adaptTo(Class<? extends Object> ifc) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean canExecute(IProperties properties) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public OperationResult doExecute(IProperties properties, Monitor monitor) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
