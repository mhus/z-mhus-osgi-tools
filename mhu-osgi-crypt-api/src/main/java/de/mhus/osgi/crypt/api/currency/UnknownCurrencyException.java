package de.mhus.osgi.crypt.api.currency;

import de.mhus.lib.errors.MException;

public class UnknownCurrencyException extends MException {

	private static final long serialVersionUID = 1L;
	public UnknownCurrencyException(Object... in) {
		super(in);
	}


}
