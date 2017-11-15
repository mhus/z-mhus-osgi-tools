package de.mhus.osgi.crypt.api.cipher;

public interface StringSplitter {

	/**
	 * Splits a string in parts. Throws exception if parts is lesser then 2 or
	 * bigger the content.
	 * 
	 * @param parts Number of parts.
	 * @param content
	 * @return
	 */
	String[] split(int parts, String content) throws SplitException;

	/**
	 * Return the name of the split method.
	 * 
	 * @return
	 */
	String getName();
	
}
