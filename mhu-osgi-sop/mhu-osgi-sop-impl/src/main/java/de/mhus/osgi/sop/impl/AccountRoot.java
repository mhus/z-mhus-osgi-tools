package de.mhus.osgi.sop.impl;

import de.mhus.lib.core.security.Account;

public class AccountRoot implements Account {

	@Override
	public String getAccount() {
		return "root";
	}

	@Override
	public boolean isValide() {
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

}
