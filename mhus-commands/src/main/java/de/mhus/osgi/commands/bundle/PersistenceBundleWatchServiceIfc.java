package de.mhus.osgi.commands.bundle;

import java.io.IOException;
import java.util.List;

public interface PersistenceBundleWatchServiceIfc {

	public void writeConfig(List<String> list) throws IOException;

	public List<String> readConfig() throws IOException;

	public void doActivate();

	
}
