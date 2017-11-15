package de.mhus.osgi.crypt.api.cipher;

import de.mhus.lib.errors.MException;

public class SplitException extends MException {

	private static final long serialVersionUID = 1L;

	public SplitException(int parts, String reason) {
		super(reason,parts);
	}
	
}
