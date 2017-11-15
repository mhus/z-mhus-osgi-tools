package de.mhus.osgi.crypt.api.cipher;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPair;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;

public interface CipherProvider {

	/**
	 * Encode content, return a string block.
	 * 
	 * @param key
	 * @param string
	 * @return
	 * @throws MException 
	 */
	PemBlock encode(PemPub key, String content) throws MException;

	String decode(PemPriv key, PemBlock encoded) throws MException;
	
	String getName();

	PemPair createKeys(IProperties properties) throws MException;
	
}
