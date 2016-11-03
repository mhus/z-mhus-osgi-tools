package de.mhus.osgi.sop.impl.aaa.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;

public class AccountFromFile extends MLog implements AccountSource {

	@Override
	public Account findAccount(String account) {
		File file = new File( "aaa/account/" + MFile.normalize(account.trim()).toLowerCase() + ".xml" );
		if (!file.exists() || !file.isFile()) {
			log().i("file not found", file);
			return null;
		}
		
		try {
			return new AccountFile(file, account);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log().w(account, e);
			return null;
		}
	}

}
