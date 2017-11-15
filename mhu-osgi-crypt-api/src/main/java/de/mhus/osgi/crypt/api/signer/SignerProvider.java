package de.mhus.osgi.crypt.api.signer;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPair;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;

public interface SignerProvider {

	/** 
	 * Create a sign of the full text.
	 * 
	 * @param key
	 * @param text
	 * @return
	 */
	PemBlock sign(PemPriv key, String text) throws MException;

	boolean validate(PemPub key, String text, PemBlock sign) throws MException;

	String getName();
	
	PemPair createKeys(IProperties properties) throws MException;
	
}
