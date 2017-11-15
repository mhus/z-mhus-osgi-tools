package de.mhus.osgi.crypt.api;

import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;

public interface BlockSign {

	/**
	 * Check Signature.
	 * 
	 * @param text
	 * @return
	 */
	boolean validate(PemPub key, String text, String sign);

	/**
	 * Create a signature. Return the result as string block
	 * 
	 * @param text
	 * @return
	 */
	String sign(PemPriv key, String text);

	/**
	 * Returns the identifier of the sign method
	 * 
	 * @return
	 */
	String getName();

}
