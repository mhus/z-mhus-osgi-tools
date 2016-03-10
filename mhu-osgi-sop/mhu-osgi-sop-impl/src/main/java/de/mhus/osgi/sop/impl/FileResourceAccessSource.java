package de.mhus.osgi.sop.impl;

import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.Account;
import de.mhus.osgi.sop.api.aaa.AuthorizationSource;

public class FileResourceAccessSource implements AuthorizationSource {

	@Override
	public Boolean hasResourceAccess(SopApi api, Account account, String mappingName, String id, String action) {

		return null;
	}

}
