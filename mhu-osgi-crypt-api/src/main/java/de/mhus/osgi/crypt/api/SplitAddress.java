package de.mhus.osgi.crypt.api;

public interface SplitAddress {

	/**
	 * Returns the public address.
	 * 
	 * @return
	 */
	String getAddress();
	
	/**
	 * Returns the specified part of the address as string block.
	 * @return
	 */
	String getPart(int index);
	
	/**
	 * Returns the size of parts the address was split.
	 * 
	 * @return
	 */
	int size();
	
	/**
	 * Returns the identifier of the splitting method
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Returns the signature of the original address.
	 * @return
	 */
	String getSign();
}
