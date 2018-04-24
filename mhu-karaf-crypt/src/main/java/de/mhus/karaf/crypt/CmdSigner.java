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

import java.util.Date;
import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockModel;
import de.mhus.lib.core.crypt.pem.PemKey;
import de.mhus.lib.core.crypt.pem.PemPair;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.util.Lorem;
import de.mhus.lib.core.vault.DefaultEntry;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.core.vault.MutableVaultSource;
import de.mhus.lib.core.vault.VaultSource;
import de.mhus.osgi.crypt.api.CryptaApi;
import de.mhus.osgi.crypt.api.signer.SignerProvider;
import de.mhus.osgi.services.MOsgi;

@Command(scope = "crypta", name = "signer", description = "Signer Handling")
@Service
public class CmdSigner extends MLog implements Action {

	@Argument(index=0, name="signer", required=true, description="Selected signer", multiValued=false)
    String signer;
	@Argument(index=1, name="cmd", required=true, description="Command:\n list\n create\n sign [key] [text]\n validate [key] [sign] [text]", multiValued=false)
    String cmd;

	@Argument(index=2, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

    @Option(name = "-i", aliases = { "--import" }, description = "Import into vault (don't forget to save vault)", required = false, multiValued = false)
    boolean imp = false;
    
    @Option(name = "-s", aliases = { "--source" }, description = "Define vault source other then 'default'", required = false, multiValued = false)
    String impSource = "default";

    @Option(name = "-d", aliases = { "--description" }, description = "Descritpion of the key", required = false, multiValued = false)
    String desc = "";
    
    @Option(name = "-p", aliases = { "--passphrase" }, description = "Define a passphrase if required", required = false, multiValued = false)
    String passphrase = null;

	@Override
	public Object execute() throws Exception {
		
		if (cmd.equals("list")) {
			for (de.mhus.osgi.services.MOsgi.Service<SignerProvider> ref : MOsgi.getServiceRefs(SignerProvider.class, null)) {
				System.out.println(ref.getReference().getProperty("signer"));
			}
			return null;
		}

		SignerProvider prov = MApi.lookup(CryptaApi.class).getSigner(signer);

		switch (cmd) {
		case "create": {
			MProperties p = MProperties.explodeToMProperties(parameters);
			if (passphrase != null)
				p.setString("passphrase", passphrase);
			PemPair keys = prov.createKeys(p);
			PemPriv priv = keys.getPrivate();
			PemPub pub = keys.getPublic();

			Date now = new Date();
			if (priv instanceof PemKey) {
				if (MString.isSet(desc))
					((PemKey)priv).setString(PemBlock.DESCRIPTION, desc);
				((PemKey)priv).setDate(PemBlock.CREATED, now);
			}
			if (pub instanceof PemKey) {
				if (MString.isSet(desc))
					((PemKey)pub).setString(PemBlock.DESCRIPTION, desc);
				((PemKey)pub).setDate(PemBlock.CREATED, now);
			}
			
			System.out.println(new PemKey((PemKey)priv, false));
			System.out.println(pub);
			System.out.println("Private: " + PemUtil.toLine(priv));
			System.out.println();
			System.out.println("Public : " + PemUtil.toLine(pub ));
			
			if (imp) {
				MVault vault = MVaultUtil.loadDefault();
				VaultSource vaultSource = vault.getSource(impSource);
				if (vaultSource == null) {
					System.out.println("Vault Source not found " + impSource);
				} else {
					if (vaultSource instanceof MutableVaultSource) {
						
						DefaultEntry pubEntry = new DefaultEntry((UUID)pub.get(PemBlock.IDENT), MVault.TYPE_RSA_PUBLIC_KEY, desc, pub.toString() );
						DefaultEntry privEntry = new DefaultEntry((UUID)priv.get(PemBlock.IDENT), MVault.TYPE_RSA_PRIVATE_KEY, desc, 
								new PemKey((PemKey)priv, false).toString() 
								);
						
						MutableVaultSource mvs = (MutableVaultSource)vaultSource;
						mvs.addEntry(pubEntry);
						mvs.addEntry(privEntry);
						
						System.out.println("IMPORTED!");
					} else {
						System.out.println("Vault source is not writable " + impSource);
					}
				}
			}

			return new Object[] {priv,pub};
		} 
		case "sign": {
			String text = parameters[1];
			PemPriv key = PemUtil.signPrivFromString(parameters[0]);
			PemBlock res = prov.sign(key, text, passphrase);
			System.out.println(res);
			return res;
		}
		case "validate": {
			String text = parameters[2];
			PemPub key = PemUtil.signPubFromString(parameters[0]);
			PemBlock sign = findTextBlock(parameters[1]);
			
			boolean res = prov.validate(key, text, sign);
			System.out.println("Validate: " + res);
			return res;
		}
		case "test": {
			MProperties p = MProperties.explodeToMProperties(parameters);
			if (passphrase != null)
				p.setString("passphrase", passphrase);
			String text = Lorem.create(p.getInt("lorem", 1));
			
			PemPair keys = prov.createKeys(p);
			System.out.println(keys);
			
			PemBlock sign = prov.sign(keys.getPrivate(), text, passphrase);
			System.out.println(sign);
			
			boolean valide = prov.validate(keys.getPublic(), text, sign);
			System.out.println("Valide: " + valide);
			
		} break;
		default:
			System.out.println("Command unknown");
		}
		return null;
	}

	private PemBlock findTextBlock(String str) throws Exception {
		if (PemUtil.isPemBlock(str)) {
			return new PemBlockModel().parse(str);
		}
		return new PemBlockModel("", str );
	}
	
}
