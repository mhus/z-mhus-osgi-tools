package de.mhus.osgi.commands.watch;

import java.io.IOException;

import de.mhus.lib.errors.NotFoundException;

public interface PersistentWatch {
	
	void add(String line) throws IOException;
	void remove(String line) throws IOException;
	String[] list() throws IOException;
	void watch();
	void clear() throws IOException;
	void remember() throws IOException, NotFoundException;
	
}
