package de.mhus.osgi.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.operation.Operation;
import de.mhus.osgi.api.services.OperationManager;
import de.mhus.osgi.api.util.MServiceTracker;

@Component(immediate = true)
public class OperationManagerImpl extends MLog implements OperationManager {

	MServiceTracker<Operation> operationTracker = 
			new MServiceTracker<>(
					Operation.class,
            		(reference, service) -> add(reference, service),
            		(reference, service) -> remove(reference, service)
					);

	private Map<String, Operation> operationByPathVersion = Collections.synchronizedMap(new HashMap<>());
	private Map<UUID, Operation> operationById = Collections.synchronizedMap(new HashMap<>());

	@Activate
	public void doActivate(ComponentContext ctx) {
		operationTracker.start(ctx);
	}
	
	@Deactivate
	public void doDeactivate() {
		operationTracker.stop();
	}
	
	private void remove(ServiceReference<Operation> reference, Operation service) {
		operationByPathVersion.remove(service.getDescription().getPathVersion());
		operationById.remove(service.getDescription().getUuid());
	}

	private void add(ServiceReference<Operation> reference, Operation service) {
		operationByPathVersion.put(service.getDescription().getPathVersion(), service);
		operationById.put(service.getDescription().getUuid(), service);
	}

	@Override
	public Operation getOperation(String name) {
		if (name == null) return null;
		if (MValidator.isUUID(name))
			return operationById.get(UUID.fromString(name));
		return operationByPathVersion.get(name);
	}

	@Override
	public List<Operation> getOperations() {
		return new ArrayList<>(operationById.values());
	}

}
