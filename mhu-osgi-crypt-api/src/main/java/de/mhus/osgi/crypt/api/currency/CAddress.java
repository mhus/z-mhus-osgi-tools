/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.crypt.api.currency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CAddress implements Externalizable {

	protected String currency;
	protected String address;
	protected String privKey;

	public CAddress() {
	}
	
	public CAddress(CCurrency currency, String addr) {
		this.address = addr;
		this.currency = currency.toString();
	}
	
	public CAddress(CCurrency currency, String addr, String privKey) {
		this.address = addr;
		this.privKey = privKey;
		this.currency = currency.toString();
	}

	public CAddress(String currency, String addr) {
		this.address = addr;
		this.currency = currency.toUpperCase();
	}

	/**
	 * Return private address or null if unknown.
	 * 
	 * @return the private key
	 */
	public final String getPrivate() {
		return privKey;
	}

	/**
	 * Return the public address in the format it is used in the wallets (e.g. compressed).
	 * 
	 * @return the address
	 */
	public final String getAddress() {
		return address;
	}

	/**
	 * Remove all private informations inside of the object. You can call the 
	 * method multiple times without error even if the address is already secure.
	 */
	public final void doSecure() {
		privKey = null;
	}
	
	/**
	 * Return true if it's secure to give the object away to non secure areas.
	 * @return true if the private key is a secret
	 */
	public final boolean isSecure() {
		return privKey == null;
	}
	
	@Override
	public String toString() {
		return currency + ":" + address;
	}
	
	@Override
	public boolean equals(Object in) {
		if (address == null || in == null || !(in instanceof CAddress)) return false;
		return address.equals( ((CAddress)in).getAddress() ); // did not check currency type
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(address);
		out.writeObject(privKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		address = (String) in.readObject();
		privKey = (String) in.readObject();
	}

}
