package de.mhus.osgi.sop.impl;


public class ContextPool {

	private static ContextPool instance;
	private ThreadLocal<AaaContextImpl> pool = new ThreadLocal<>();
	
	public synchronized static ContextPool getInstance() {
		if (instance == null)
			instance = new ContextPool();
		return instance;
	}
	
	public AaaContextImpl getCurrent() {
		synchronized (pool) {
			return pool.get();
		}
	}
	
	public void set(AaaContextImpl context) {
		synchronized (pool) {
			AaaContextImpl parent = pool.get();
			if (context != null) {
				context.setParent(parent);
				pool.set(context);
			} else {
				pool.remove();
			}
		}
	}
	
	
}
