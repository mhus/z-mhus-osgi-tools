package de.mhus.osgi.sop.impl;

import de.mhus.osgi.sop.api.SopApi;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AuthorizationSource;

public class FileResourceAccessSource implements AuthorizationSource {

	@Override
	public Boolean hasResourceAccess(Account account, String mappingName, String id, String action) {

		return null;
	}

}
