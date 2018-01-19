package de.mhus.osgi.crypt.bc;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.UUID;

import javax.crypto.Cipher;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
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

@Component(properties="cipher=RSA-1") // Default RSA
public class JavaRsaCipher extends MLog implements CipherProvider {

	private final String NAME = "RSA-1";

	@Override
	public PemBlock encode(PemPub key, String content) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			
			String stringEncoding = "utf-8";
			byte[] b = content.getBytes(stringEncoding);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			int off = 0;
			while (off < b.length) {
				int len = Math.min(117, b.length - off);
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
	public String decode(PemPriv key, PemBlock encoded) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			
			byte[] b = encoded.getBytesBlock();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			int off = 0;
			while (off < b.length) {
				int len = Math.min(128, b.length - off);
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
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			MRandom random = MApi.lookup(MRandom.class);
			keyGen.initialize(len, random.getSecureRandom());
			
			KeyPair    pair = keyGen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey  pub  = pair.getPublic();
			
			UUID privId = UUID.randomUUID();
			UUID pubId = UUID.randomUUID();

			PemKey xpub  = new PemKey(PemBlock.BLOCK_PUB , pub.getEncoded(), false  )
					.set(PemBlock.METHOD, getName())
					.set(PemBlock.LENGTH, len)
					.set(PemBlock.FORMAT, pub.getFormat())
					.set(PemBlock.IDENT, pubId)
					.set(PemBlock.PRIV_ID, privId);
			PemKey xpriv = new PemKey(PemBlock.BLOCK_PRIV, priv.getEncoded(), true )
					.set(PemBlock.METHOD, getName())
					.set(PemBlock.LENGTH, len)
					.set(PemBlock.FORMAT, priv.getFormat())
					.set(PemBlock.IDENT, privId)
					.set(PemBlock.PUB_ID, pubId);
			
			return new PemKeyPair(xpriv, xpub);
			
		} catch (Exception e) {
			throw new MException(e);
		}
	}

}
