package de.mhus.osgi.crypt.api;

import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;

public interface BlockCrypt {

	/**
	 * Encodes a string block. Returns the result as string block.
	 * 
	 * @param text
	 * @return
	 */
	String encode(PemPub key, String text);

	/**
	 * Decodes a string block.
	 * 
	 * @param text
	 * @return
	 */
	String decode(PemPriv key, String text);

	/**
	 * Returns the identifier of the crypt method
	 * 
	 * @return
	 */
	String getName();

}
