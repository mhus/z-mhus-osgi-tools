package de.mhus.osgi.sop.impl.aaa.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.aaa.TrustSource;

public class TrustFromFile extends MLog implements TrustSource {

	@Override
	public Trust findTrust(String trust) {
		File file = new File( "aaa/trust/" + MFile.normalize(trust.trim()).toLowerCase() + ".xml" );
		if (!file.exists() || !file.isFile()) return null;
		
		try {
			return new TrustFile(file, trust);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log().w(trust, e);
			return null;
		}
	}

	@Override
	public String createTrustTicket(AaaContext user) {
		return null;
	}

}
