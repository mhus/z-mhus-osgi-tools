package de.mhus.osgi.crypt.api.error;

import de.mhus.lib.errors.MException;

public class UnknownCurrency extends MException {

	private static final long serialVersionUID = 1L;
	public UnknownCurrency(Object... in) {
		super(in);
	}


}
