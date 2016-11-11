package de.mhus.osgi.sop.api.aaa;

import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;

public interface AaaContext {

	Account getAccount();
	Trust getTrust();
	boolean isAdminMode();
	String getAccountId();
	ContextCachedItem getCached(String key);
	void setCached(String key, ContextCachedItem item);
}
