package de.mhus.osgi.sop.impl.aaa.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AuthorizationSource;
import de.mhus.osgi.sop.api.aaa.AaaUtil;

public class AuthFromFile extends MLog implements AuthorizationSource {

	@Override
	public Boolean hasResourceAccess(Account account, String aclName) {
		File file = new File( "aaa/groupmapping/" + MFile.normalize(aclName.trim()).toLowerCase() + ".txt" );
		if (!file.exists()) {
			log().w("file not found",file);
			return null;
		}
		try {
			List<String> acl = MFile.readLines(file, true);
			return AaaUtil.hasAccess(account, acl);
		} catch (IOException e) {
			log().w("read error", file);
			return null;
		}
	}

}
