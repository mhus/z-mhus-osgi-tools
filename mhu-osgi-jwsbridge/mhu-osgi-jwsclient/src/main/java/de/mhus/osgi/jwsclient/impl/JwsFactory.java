package de.mhus.osgi.jwsclient.impl;

import java.io.IOException;

import de.mhus.osgi.jwsclient.Client;
import de.mhus.osgi.jwsclient.Target;
import de.mhus.osgi.jwsclient.TargetFactory;

public class JwsFactory implements TargetFactory {

	@Override
	public Target createTarget(Client jwsClient, String[] parts)
			throws IOException {
		return new JwsTarget(this, jwsClient, parts);
	}

}
