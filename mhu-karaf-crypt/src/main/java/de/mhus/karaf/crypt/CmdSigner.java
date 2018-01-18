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
import de.mhus.osgi.crypt.api.signer.SignerProvider;

@Command(scope = "crypta", name = "signer", description = "Signer Handling")
@Service
public class CmdSigner extends MLog implements Action {

	@Argument(index=0, name="signer", required=true, description="Selected signer", multiValued=false)
    String signer;
	@Argument(index=1, name="cmd", required=true, description="Command:\n list\n create\n sign [key] [text]\n validate [key] [sign] [text]", multiValued=false)
    String cmd;

	@Argument(index=2, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

	@Override
	public Object execute() throws Exception {
		
		if (cmd.equals("list")) {
			for (de.mhus.lib.karaf.MOsgi.Service<SignerProvider> ref : MOsgi.getServiceRefs(SignerProvider.class, null)) {
				System.out.println(ref.getReference().getProperty("signer"));
			}
			return null;
		}

		SignerProvider prov = MApi.lookup(CryptaApi.class).getSigner(signer);

		switch (cmd) {
		case "create": {
			PemPair keys = prov.createKeys(MProperties.explodeToMProperties(parameters));
			PemPriv priv = keys.getPrivate();
			PemPub pub = keys.getPublic();
			System.out.println(new PemKey(priv.getName(), priv.getBlock(), false));
			System.out.println(pub);
			System.out.println("Private: " + PemUtil.toLine(priv));
			System.out.println();
			System.out.println("Public : " + PemUtil.toLine(pub ));
			return new Object[] {priv,pub};
		} 
		case "sign": {
			String text = parameters[1];
			PemPriv key = PemUtil.signPrivFromString(parameters[0]);
			PemBlock res = prov.sign(key, text);
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
			String text = parameters == null || parameters.length < 1 ? Lorem.create() : parameters[0];
			
			PemPair keys = prov.createKeys(null);
			System.out.println(keys);
			
			PemBlock sign = prov.sign(keys.getPrivate(), text);
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
