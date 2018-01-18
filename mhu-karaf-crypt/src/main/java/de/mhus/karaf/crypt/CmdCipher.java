package de.mhus.karaf.crypt;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockModel;
import de.mhus.lib.core.crypt.pem.PemKey;
import de.mhus.lib.core.crypt.pem.PemPair;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.util.Lorem;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.osgi.crypt.api.CryptaApi;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;

@Command(scope = "crypta", name = "cipher", description = "Cipher Handling")
@Service
public class CmdCipher extends MLog implements Action {

	@Argument(index=0, name="cipher", required=true, description="Selected cipher", multiValued=false)
    String cipher;
	@Argument(index=1, name="cmd", required=true, description="Command:\n list\n encode [key] [text]\n decode [key] [encoded]\n create", multiValued=false)
    String cmd;

	@Argument(index=2, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

	@Override
	public Object execute() throws Exception {

		if (cmd.equals("list")) {
			for (de.mhus.lib.karaf.MOsgi.Service<CipherProvider> ref : MOsgi.getServiceRefs(CipherProvider.class, null)) {
				System.out.println(ref.getReference().getProperty("cipher"));
			}
			return null;
		}

		CipherProvider prov = MApi.lookup(CryptaApi.class).getCipher(cipher);

		switch (cmd) {
		case "encode": { 
			String text = parameters[1];
			PemPub key = PemUtil.cipherPubFromString(parameters[0]);
			PemBlock res = prov.encode(key, text);
			System.out.println(res);
			return res;
		}
		case "decode": { 
			PemBlock text = findEncodedBlock(parameters[1]);
			PemPriv key = PemUtil.cipherPrivFromString(parameters[0]);
			String res = prov.decode(key, text);
			System.out.println(res);
			return res;
		}
		case "create": {
			PemPair keys = prov.createKeys(MProperties.explodeToMProperties(parameters));
			PemPriv priv = keys.getPrivate();
			PemPub pub = keys.getPublic();
			System.out.println(new PemKey(priv.getName(), priv.getBlock(), false)); // need to create a new key without security restriction
			System.out.println(pub);
			System.out.println("Private: " + PemUtil.toLine(priv));
			System.out.println();
			System.out.println("Public : " + PemUtil.toLine(pub ));
			return new Object[] {priv,pub};
		}
		case "test": {
			String text = parameters == null || parameters.length < 1 ? Lorem.create() : parameters[0];
			System.out.println(text);
			PemPair keys = prov.createKeys(null);
			System.out.println(keys);
			PemBlock encoded = prov.encode(keys.getPublic(), text);
			System.out.println(encoded);
			String decoded = prov.decode(keys.getPrivate(), encoded);
			System.out.println(decoded);
			boolean valid = text.equals(decoded);
			System.out.println("Valide: " + valid);
			
		} break;
		default:
			System.out.println("Command unknown");
		}
		return null;
	}

	private static PemBlock findEncodedBlock(String text) throws Exception {

		PemBlockModel block = new PemBlockModel().parse(text);
		return block;
	}
}
