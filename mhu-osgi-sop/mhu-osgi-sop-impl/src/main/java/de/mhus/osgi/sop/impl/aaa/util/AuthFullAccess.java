package de.mhus.osgi.sop.impl.aaa.util;

import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AuthorizationSource;

public class AuthFullAccess implements AuthorizationSource {

	@Override
	public Boolean hasResourceAccess(Account account, String aclName) {
		return true;
	}

}
