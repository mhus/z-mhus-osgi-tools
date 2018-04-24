/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.crypt;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.CryptaApi;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;
import de.mhus.osgi.crypt.api.currency.CurrencyProvider;
import de.mhus.osgi.crypt.api.signer.SignerProvider;
import de.mhus.osgi.services.MOsgi;

@Component
public class CryptaApiImpl extends MLog implements CryptaApi {

	
	private static final String DEFAULT_SIGN = "BCFIPS-1";
	private static final String DEFAUL_CIPHER = "BCFIPS-1";
	private CipherProvider cipher;
	private SignerProvider signer;

	@Override
	public CipherProvider getCipher(String cipher) throws NotFoundException {
		cipher = normalizeName(cipher);
		return MOsgi.getService(CipherProvider.class, "(cipher="+cipher+")");
	}

	@Override
	public CipherProvider getDefaultCipher() throws NotFoundException {
		if (cipher == null)
			cipher = getCipher(DEFAUL_CIPHER);
		return cipher;
	}

	@Override
	public PemBlock sign(PemPriv key, String text, String passphrase) throws MException {
		SignerProvider sign = getSigner(key.getMethod());
		return sign.sign(key, text, passphrase);
	}
	
	@Override
	public boolean validate(PemPub key, String text, PemBlock sign) throws MException {
		SignerProvider s = getSigner(key.getMethod());
		return s.validate(key, text, sign);
	}
	
	@Override
	public SignerProvider getDefaultSigner() throws NotFoundException {
		if (signer == null)
			signer = getSigner(DEFAULT_SIGN);
		return signer;
	}

	@Override
	public SignerProvider getSigner(String signer) throws NotFoundException {
		signer = normalizeName(signer);
		return MOsgi.getService(SignerProvider.class, "(signer="+signer+")");
	}

	@Override
	public CurrencyProvider getCurrency(String currency) throws NotFoundException {
		currency = normalizeName(currency);
		return MOsgi.getService(CurrencyProvider.class, "(currency="+currency+")");
	}

	private String normalizeName(String currency) {
		return currency.trim().toUpperCase();
	}
	
}
