package de.mhus.osgi.sop.impl.aaa;

import de.mhus.osgi.sop.impl.AaaContextImpl;

public class RootContext extends AaaContextImpl {

	public RootContext() {
		super(new AccountRoot());
		adminMode = true;
	}
	
	public void setAdminMode(boolean admin) {
		adminMode = admin;
	}

}
