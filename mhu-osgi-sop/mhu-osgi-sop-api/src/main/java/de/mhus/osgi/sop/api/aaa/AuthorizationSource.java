package de.mhus.osgi.sop.api.aaa;

import de.mhus.osgi.sop.api.SopApi;

public interface AuthorizationSource {
	
	/**
	 * Return null if the mapping was not found, true or false if there is a concrete result.
	 * @param api
	 * @param account
	 * @param mappingName
	 * @param id
	 * @param action
	 * @return
	 */
	Boolean hasResourceAccess(SopApi api, Account account, String mappingName, String id, String action);
}
