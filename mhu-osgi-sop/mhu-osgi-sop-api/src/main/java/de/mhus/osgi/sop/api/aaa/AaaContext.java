package de.mhus.osgi.sop.api.aaa;

import de.mhus.lib.errors.MException;

public interface AaaContext {

	Account getAccount() throws MException;
	Trust getTrust();
	boolean isAdminMode();
	String getAccountId();
	ContextCachedItem getCached(String key);
	void setCached(String key, ContextCachedItem item);
}
