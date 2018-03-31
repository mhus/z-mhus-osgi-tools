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
package de.mhus.osgi.crypt.api;

import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;
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
