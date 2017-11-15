package de.mhus.osgi.crypt.api.currency;

public interface CAddress {

	/**
	 * Return private address or null if unknown.
	 * 
	 * @return
	 */
	String getPrivate();

	/**
	 * Return the public address in the format it is used in the wallets (e.g. compressed).
	 * 
	 * @return
	 */
	String getAddress();

	/**
	 * Remove all private informations inside of the object. You can call the 
	 * method multiple times without error even if the address is already secure.
	 */
	void doSecure();
	
	/**
	 * Return true if it's secure to give the object away to non secure areas.
	 * @return
	 */
	boolean isSecure();
}
