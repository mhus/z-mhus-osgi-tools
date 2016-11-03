package de.mhus.osgi.sop.api.aaa;

import de.mhus.lib.core.IProperties;

public interface Trust {

	public String getTrust();

	public boolean isValid();

	public boolean validatePassword(String password);

	public boolean isChanged();

	IProperties getProperties();

	String getName();

}
