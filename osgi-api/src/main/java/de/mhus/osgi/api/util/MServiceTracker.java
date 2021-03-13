package de.mhus.osgi.api.util;

import java.util.function.BiConsumer;

import org.osgi.framework.ServiceReference;

public class MServiceTracker<T> extends AbstractServiceTracker<T>{

	private BiConsumer<ServiceReference<T>, T> addAction;
	private BiConsumer<ServiceReference<T>, T> removeAction;

	public MServiceTracker(Class<T> clazz, BiConsumer<ServiceReference<T>, T> addAction, BiConsumer<ServiceReference<T>, T> removeAction ) {
		super(clazz);
		this.addAction = addAction;
		this.removeAction = removeAction;
	}

	@Override
	protected void removeService(ServiceReference<T> reference, T service) {
		removeAction.accept(reference, service);
	}

	@Override
	protected void addService(ServiceReference<T> reference, T service) {
		addAction.accept(reference, service);
	}

}
