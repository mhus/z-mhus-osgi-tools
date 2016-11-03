package de.mhus.osgi.sop.api.aaa;

import java.util.HashSet;
import java.util.Set;

import de.mhus.lib.core.security.Account;

public class AccountGuest implements Account {

	private String name = "Guest";
	private HashSet<String> groups = new HashSet<>();

	@Override
	public String getName() {
		return "guest";
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean validatePassword(String password) {
		return true;
	}

	public String toString() {
		return "guest";
	}

	@Override
	public boolean isSyntetic() {
		return true;
	}

	@Override
	public String getDisplayName() {
		return name;
	}

	public void setDisplayName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean hasGroup(String group) {
		return groups.contains(group);
	}

	public Set<String> getGroups() {
		return groups;
	}
}
