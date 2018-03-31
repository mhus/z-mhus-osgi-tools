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

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.UUID;

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
import de.mhus.osgi.crypt.api.signer.SignerProvider;

// http://bouncycastle.org/wiki/display/JA1/Elliptic+Curve+Key+Pair+Generation+and+Key+Factories

@Component(properties="signer=ECC-1")
public class EccSigner extends MLog implements SignerProvider {

	@Activate
	public void doActivate(ComponentContext ctx) {
		BouncyUtil.init();
	}
	
	@Override
	public PemBlock sign(PemPriv key, String text, String passphrase) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			if (passphrase != null)
				encKey = Blowfish.decrypt(encKey, passphrase);
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
			PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
			
			Signature dsa = Signature.getInstance("SHA512WITHECDSA", "BC"); 
			dsa.initSign(privKey);
			byte[] buffer = text.getBytes();
			dsa.update(buffer, 0, buffer.length);
			
			byte[] realSig = dsa.sign();
			
			PemBlockModel out = new PemBlockModel(PemBlock.BLOCK_SIGN, realSig).set(PemBlock.METHOD,getName());
			if (key.isProperty(PemBlock.IDENT))
				out.set(PemBlock.KEY_IDENT, key.getProperty(PemBlock.IDENT));
			out.set(PemBlock.CREATED, new Date());
			
			return out;
		} catch (Exception e) {
			throw new MException(e);
		}
	}

	@Override
	public boolean validate(PemPub key, String text, PemBlock sign) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			
			Signature sig = Signature.getInstance("SHA512WITHECDSA", "BC");
			sig.initVerify(pubKey);
			
			byte[] buffer = text.getBytes();
			sig.update(buffer, 0, buffer.length);

			byte[] sigToVerify = sign.getBytesBlock();
			return sig.verify(sigToVerify);
			
		} catch (Exception e) {
			throw new MException(e);
		}
	}

	@Override
	public String getName() {
		return "ECC-1";
	}

	@Override
	public PemPair createKeys(IProperties properties) throws MException {
		try {
//			EllipticCurve curve = new EllipticCurve(
//		            new ECFieldFp(new BigInteger("883423532389192164791648750360308885314476597252960362792450860609699839")), // q
//		            new BigInteger("7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc", 16), // a            
//		            new BigInteger("6b016c3bdcf18941d0d654921475ca71a9db2fb27d1d37796185c2942c0a", 16)); // b
//			ECParameterSpec ecSpec = new ECParameterSpec(
//			            curve,
//			            ECPointUtil.decodePoint(curve, Hex.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf")), // G
//			            new BigInteger("883423532389192164791648750360308884807550341691627752275345424702807307"), // n
//			            1); // h
//			KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
//			g.initialize(ecSpec, random.getSecureRandom());
//			KeyPair pair = g.generateKeyPair();
			if (properties == null) properties = new MProperties();

			String stdName = properties.getString("stdName", "prime192v1");
			ECGenParameterSpec     ecGenSpec = new ECGenParameterSpec(stdName);
			KeyPairGenerator    g = KeyPairGenerator.getInstance("ECDSA", "BC");
			MRandom random = MApi.lookup(MRandom.class);
			g.initialize(ecGenSpec, random.getSecureRandom());
			KeyPair pair = g.generateKeyPair();
			
			PrivateKey priv = pair.getPrivate();
			
			PublicKey pub = pair.getPublic();
			
			UUID privId = UUID.randomUUID();
			UUID pubId = UUID.randomUUID();

			byte[] privBytes = priv.getEncoded();
			String passphrase = properties.getString("passphrase", null);
			if (passphrase != null)
				privBytes = Blowfish.encrypt(privBytes, passphrase);

			PemKey xpub  = new PemKey(PemBlock.BLOCK_PUB , pub.getEncoded(), false  )
					.set(PemBlock.METHOD, getName())
					.set("StdName", stdName)
					.set(PemBlock.FORMAT, pub.getFormat())
					.set(PemBlock.IDENT, pubId)
					.set(PemBlock.PRIV_ID, privId);
			PemKey xpriv = new PemKey(PemBlock.BLOCK_PRIV, privBytes, true )
					.set(PemBlock.METHOD, getName())
					.set("StdName", stdName)
					.set(PemBlock.FORMAT, priv.getFormat())
					.set(PemBlock.IDENT, privId)
					.set(PemBlock.PUB_ID, pubId);
			
			if (passphrase != null)
				xpriv.set(PemBlock.ENCRYPTED, PemBlock.ENC_BLOWFISH);
			privBytes = null;
			return new PemKeyPair(xpriv, xpub);

		} catch (Throwable t) {
			throw new MException(t);
		}
	}

}

/*
 * 
The following ECDSA curves are currently supported by the Bouncy Castle APIs:
F p
X9.62
Curve	Size (in bits)
prime192v1
192
prime192v2	192
prime192v3	192
prime239v1	239
prime239v2	239
prime239v3	239
prime256v1	256
SEC
Curve
Size (in bits)
secp192k1	192
secp192r1	192
secp224k1	224
secp224r1	224
secp256k1	256
secp256r1	256
secp384r1	384
secp521r1	521
NIST (aliases for SEC curves)
Curve
Size (in bits)
P-224	224
P-256	256
P-384	384
P-521	521
F 2m
X9.62
Curve
Size (in bits)
c2pnb163v1	163
c2pnb163v2	163
c2pnb163v3	163
c2pnb176w1	176
c2tnb191v1	191
c2tnb191v2	
191
c2tnb191v3	191
c2pnb208w1	208
c2tnb239v1	239
c2tnb239v2	239
c2tnb239v3	239
c2pnb272w1	272
c2pnb304w1	304
c2tnb359v1	359
c2pnb368w1	368
c2tnb431r1	431
SEC
Curve
Size (in bits)
sect163k1	163
sect163r1	163
sect163r2	163
sect193r1	193
sect193r2	193
sect233k1	233
sect233r1	233
sect239k1	239
sect283k1	283
sect283r1	283
sect409k1	409
sect409r1	409
sect571k1	571
sect571r1	571
NIST (aliases for SEC curves)
Curve
Size (in bits)
B-163	
163
B-233	233
B-283	283
B-409	409
B-571	571
Teletrust
Curve
Size (in bits)
brainpoolp160r1	160
brainpoolp160t1	160
brainpoolp192r1	192
brainpoolp192t1	192
brainpoolp224r1	224
brainpoolp224t1	224
brainpoolp256r1	256
brainpoolp256t1	256
brainpoolp320r1	320
brainpoolp320t1	320
brainpoolp384r1	384
brainpoolp384t1	384
brainpoolp512r1	512
brainpoolp512t1	512
Supported ECGOST (GOST3410-2001) Curves
The following ECGOST curves are currently supported by the Bouncy Castle APIs:
Curve
GostR3410-2001-CryptoPro-A
GostR3410-2001-CryptoPro-XchB
GostR3410-2001-CryptoPro-XchA
GostR3410-2001-CryptoPro-C
GostR3410-2001-CryptoPro-B

 * 
 */
