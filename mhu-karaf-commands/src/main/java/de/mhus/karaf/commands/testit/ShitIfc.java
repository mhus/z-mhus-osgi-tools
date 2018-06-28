package de.mhus.karaf.commands.testit;

public interface ShitIfc {

	void printUsage();

	Object doExecute(String cmd, String[] parameters) throws Exception;

}
