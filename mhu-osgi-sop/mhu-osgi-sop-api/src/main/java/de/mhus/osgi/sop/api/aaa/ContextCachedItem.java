package de.mhus.osgi.sop.api.aaa;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;


public class ContextCachedItem {

	public ContextCachedItem() {}
	public ContextCachedItem(Object obj) {
		setObject(obj);
	}

	public ContextCachedItem(long ttl) {
		this.ttl = ttl;
	}
	
	private long ttl = 1000 * 60;
	private long created = System.currentTimeMillis();
	
	public boolean bool;
	public int integer;
	private Object object;
	private Bundle bundle;
	private long modified;
	
	public boolean isValid() {
		if (System.currentTimeMillis() - created > ttl)
			return false;

		if (bundle != null && modified != bundle.getLastModified())
			return false;
		
		return true;
	}
	
	public void invalidate() {
		ttl = 0;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		if (object != null) {
			this.bundle = FrameworkUtil.getBundle(object.getClass());
			this.modified = bundle.getLastModified();
		}
		this.object = object;
	}

}
