package de.mhus.osgi.bundlewatch;

import java.io.IOException;
import java.util.List;

public interface PersistenceBundleWatchServiceIfc {

	public void writeConfig(List<String> list) throws IOException;

	public List<String> readConfig() throws IOException;

	public void doActivate();

	
}
