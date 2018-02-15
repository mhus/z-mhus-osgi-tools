package de.mhus.osgi.crypt.api.currency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CCurrency implements Externalizable {

	public static final String BTC = "BTC";
	public static final String EUR = "EUR";
	public static final String USD = "USD";
	public static final String LTC = "LTC";
	private String name;

	public CCurrency(String in) {
		name = in.trim().toUpperCase();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object in) {
		if (in == null) return false; 
		return name.equals(in.toString());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
	}
	
}
