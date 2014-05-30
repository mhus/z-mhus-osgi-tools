package de.mhus.osgi.jwsclient.standalone;

import java.io.IOException;

import de.mhus.osgi.jwsclient.Client;
import de.mhus.osgi.jwsclient.Target;
import de.mhus.osgi.jwsclient.TargetFactory;

public class JwsStandaloneClient extends Client {

	private static JwsStandaloneClient instance;

	private JwsStandaloneClient() {}
	
	public static synchronized JwsStandaloneClient instance() {
		if (instance == null)
			instance = new JwsStandaloneClient();
		return instance;
	}
	
	@Override
	public Target createTarget(String url) throws IOException {
		return super.createTarget(url);
	}
	
	@Override
	public void registerFactory(String name, TargetFactory factory) {
		super.registerFactory(name, factory);
	}


	
}
