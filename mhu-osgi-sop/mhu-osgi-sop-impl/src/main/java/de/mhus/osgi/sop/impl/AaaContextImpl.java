package de.mhus.osgi.sop.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.ContextCachedItem;
import de.mhus.osgi.sop.api.aaa.Trust;

public class AaaContextImpl implements AaaContext {

	protected AaaContextImpl parent;
	private Account account;
	protected boolean adminMode = false;
	protected SoftHashMap<String, ContextCachedItem> cache = new SoftHashMap<String, ContextCachedItem>();
	private Trust trust;

	public AaaContextImpl(Account account) {
		this.account = account;
	}
	public AaaContextImpl(Account account, Trust trust, boolean admin) throws MException {
		this.account = account;
		this.trust = trust;
		if (admin) {
			SopApi aa = Sop.getApi(SopApi.class);
			if (aa.hasResourceAccess(account, Account.MAP_ADMIN, null, null))
				adminMode = true;
		}
	}
	public AaaContextImpl getParent() {
		return parent;
	}

	public String toString() {
		return account + (adminMode ? "(admin)" : "" ) + "@AaaContext";
	}

	public Account getAccount() throws MException {
		return account;
	}

	public boolean isAdminMode() {
		return adminMode;
	}

	public void setParent(AaaContextImpl parent) {
		this.parent = parent;
	}

	@Override
	public String getAccountId() {
		try {
			return getAccount().getAccount();
		} catch (MException e) {
		}
		return null;
	}
	
	public ContextCachedItem getCached(String key) {
		if (key == null) return null;
		synchronized (cache) {
			ContextCachedItem ret = cache.get(key);
			if (ret != null ) {
				if (ret.isValid())
					return ret;
				else
					cache.remove(key);
			}
			return null;
		}
	}
	public void setCached(String key, ContextCachedItem item) {
		if (key == null || item == null) return;
		synchronized (cache) {
			cache.put(key, item);
		}
	}

	public void clearCache() {
		cache.clear();
	}
	
	public void cleanupCache() {
		cache.cleanup();
		synchronized (cache) {
			Iterator<Entry<String, ContextCachedItem>> iterator = cache.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, ContextCachedItem> entry = iterator.next();
				if (entry.getValue() != null && !entry.getValue().isValid())
					iterator.remove();
			}
		}
	}
	
	public int cacheSize() {
		return cache.size();
	}
	@Override
	public Trust getTrust() {
		return trust;
	}
	
}
