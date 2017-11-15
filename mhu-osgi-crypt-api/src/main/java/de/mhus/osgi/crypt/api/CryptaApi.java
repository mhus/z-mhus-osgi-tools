package de.mhus.osgi.crypt.api;

import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;
import de.mhus.osgi.crypt.api.cipher.StringSplitter;
import de.mhus.osgi.crypt.api.currency.CurrencyProvider;
import de.mhus.osgi.crypt.api.signer.SignerProvider;

public interface CryptaApi {

	/**
	 * Creates and splits an address into pieces.
	 * 
	 * @param currency
	 * @param keys
	 * @return
	 * @throws MException 
	 */
	SplitAddress createSplitAddress(String currency, PemPub ... keys) throws MException;
	
	PemBlock sign(PemPriv key, String text) throws MException;

	CurrencyProvider getCurrency(String currency);

	CipherProvider getCipher(String cipher);

	StringSplitter getDefaultStringSplitter();

	CipherProvider getDefaultCipher();

	SignerProvider getDefaultSigner();

	SignerProvider getSigner(String signer);

	boolean validate(PemPub key, String text, PemBlock sign) throws MException;
	
	String[] supportedCurrencies();
	
	String[] supportedFiats();
	
}
