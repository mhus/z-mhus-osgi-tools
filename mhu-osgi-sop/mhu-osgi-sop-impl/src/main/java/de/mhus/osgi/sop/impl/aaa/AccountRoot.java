package de.mhus.osgi.sop.impl.aaa;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.NotSupportedException;

public class AccountRoot implements Account {

	private MProperties attributes = new MProperties();

	@Override
	public String getName() {
		return "root";
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean validatePassword(String password) {
		return false;
	}

	public String toString() {
		return "root";
	}

	@Override
	public boolean isSyntetic() {
		return true;
	}

	@Override
	public String getDisplayName() {
		return "Root";
	}

	@Override
	public boolean hasGroup(String group) {
		return true;
	}

	@Override
	public IReadProperties getAttributes() {
		return attributes ;
	}

	@Override
	public void putAttributes(IReadProperties properties) throws NotSupportedException {
		attributes.putReadProperties(properties);
	}

}
