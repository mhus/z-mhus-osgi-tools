package de.mhus.osgi.jwsclient;

import java.io.IOException;

public interface TargetFactory {

	Target createTarget(Client jwsClient, String[] parts) throws IOException;

}
