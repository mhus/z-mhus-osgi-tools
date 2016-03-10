package de.mhus.osgi.sop.impl;

public class RootContext extends AaaContextImpl {

	public RootContext() {
		super(new AccountRoot());
		adminMode = true;
	}

}
