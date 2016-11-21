package de.mhus.osgi.sop.api.action;

import de.mhus.lib.basics.Named;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.lang.Adaptable;
import de.mhus.lib.core.strategy.Monitor;
import de.mhus.lib.core.strategy.OperationResult;

public interface Action extends Adaptable<Object>, Named {

	boolean canExecute(IProperties properties);
	
	OperationResult doExecute( IProperties properties, Monitor monitor) throws Exception;
	
}
