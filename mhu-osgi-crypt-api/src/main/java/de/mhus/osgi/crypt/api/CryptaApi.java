package de.mhus.osgi.crypt.api;

import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;
import de.mhus.osgi.crypt.api.cipher.StringSplitter;
import de.mhus.osgi.crypt.api.currency.CurrencyProvider;
import de.mhus.osgi.crypt.api.signer.SignerProvider;

public interface CryptaApi {

	PemBlock sign(PemPriv key, String text, String passphrase) throws MException;

	CurrencyProvider getCurrency(String currency) throws MException;

	CipherProvider getCipher(String cipher) throws MException;

	CipherProvider getDefaultCipher() throws MException;

	SignerProvider getDefaultSigner() throws MException;

	SignerProvider getSigner(String signer) throws MException;

	boolean validate(PemPub key, String text, PemBlock sign) throws MException;
		
}
