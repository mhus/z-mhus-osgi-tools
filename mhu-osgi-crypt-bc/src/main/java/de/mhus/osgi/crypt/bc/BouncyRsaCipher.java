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
package de.mhus.osgi.crypt.bc;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.UUID;

import javax.crypto.Cipher;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.crypt.Blowfish;
import de.mhus.lib.core.crypt.MRandom;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockModel;
import de.mhus.lib.core.crypt.pem.PemKey;
import de.mhus.lib.core.crypt.pem.PemKeyPair;
import de.mhus.lib.core.crypt.pem.PemPair;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;

// https://bouncycastle-pgp-cookbook.blogspot.de/2013/01/generating-rsa-keys.html

@Component(properties="cipher=RSA-2",immediate=true) // Bouncycastle RSA
public class BouncyRsaCipher extends MLog implements CipherProvider {

	private final String NAME = "RSA-2";
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		BouncyUtil.init();
	}
	
	@Override
	public PemBlock encode(PemPub key, String content) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

			Cipher cipher = Cipher.getInstance("RSA", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			
			String stringEncoding = "utf-8";
			byte[] b = content.getBytes(stringEncoding);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			int length = key.getInt(PemBlock.LENGTH, 1024);
			int blockSize = length == 512 ? 53 : 117;

			int off = 0;
			while (off < b.length) {
				int len = Math.min(blockSize, b.length - off);
				byte[] cipherData = cipher.doFinal(b, off, len);
				os.write(cipherData);
				off = off + len;
			}
			
			PemBlockModel out = new PemBlockModel(PemBlock.BLOCK_CIPHER, os.toByteArray());
			out.set(PemBlock.METHOD, getName());
			out.set(PemBlock.STRING_ENCODING, stringEncoding);
			if (key.isProperty(PemBlock.IDENT))
				out.set(PemBlock.KEY_IDENT, key.getString(PemBlock.IDENT));
			out.set(PemBlock.CREATED, new Date());
			
			return out;

		} catch (Throwable t) {
			throw new MException(t);
		}
	}

	@Override
	public String decode(PemPriv key, PemBlock encoded, String passphrase) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			if (passphrase != null)
				encKey = Blowfish.decrypt(encKey, passphrase);
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

			Cipher cipher = Cipher.getInstance("RSA", "BC");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			
			byte[] b = encoded.getBytesBlock();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			int length = key.getInt(PemBlock.LENGTH, 1024);
			int blockSize = Math.max(length / 1024 * 128, 64);

			int off = 0;
			while (off < b.length) {
				int len = Math.min(blockSize, b.length - off);
				byte[] realData = cipher.doFinal(b, off, len);
				os.write(realData);
				off = off + len;
			}
			
			String stringEncoding = encoded.getString(PemBlock.STRING_ENCODING, "utf-8");
			return new String(os.toByteArray(), stringEncoding);
		
		} catch (Exception e) {
			throw new MException(e);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public PemPair createKeys(IProperties properties) throws MException {
		try {
			if (properties == null) properties = new MProperties();
			int len = properties.getInt("length", 1024);
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
			MRandom random = MApi.lookup(MRandom.class);
			keyGen.initialize(len, random.getSecureRandom());
			
			KeyPair    pair = keyGen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey  pub  = pair.getPublic();
			
			UUID privId = UUID.randomUUID();
			UUID pubId = UUID.randomUUID();

			byte[] privBytes = priv.getEncoded();
			String passphrase = properties.getString("passphrase", null);
			if (passphrase != null)
				privBytes = Blowfish.encrypt(privBytes, passphrase);

			PemKey xpub  = new PemKey(PemBlock.BLOCK_PUB , pub.getEncoded(), false  )
					.set(PemBlock.METHOD, getName())
					.set(PemBlock.LENGTH, len)
					.set(PemBlock.FORMAT, pub.getFormat())
					.set(PemBlock.IDENT, pubId)
					.set(PemBlock.PRIV_ID, privId);
			PemKey xpriv = new PemKey(PemBlock.BLOCK_PRIV, privBytes, true )
					.set(PemBlock.METHOD, getName())
					.set(PemBlock.LENGTH, len)
					.set(PemBlock.FORMAT, priv.getFormat())
					.set(PemBlock.IDENT, privId)
					.set(PemBlock.PUB_ID, pubId);
			
			if (passphrase != null)
				xpriv.set(PemBlock.ENCRYPTED, PemBlock.ENC_BLOWFISH);
			privBytes = null;
			return new PemKeyPair(xpriv, xpub);
			
		} catch (Exception e) {
			throw new MException(e);
		}
	}

}
