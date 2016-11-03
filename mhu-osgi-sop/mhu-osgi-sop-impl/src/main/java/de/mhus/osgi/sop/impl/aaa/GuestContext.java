package de.mhus.osgi.sop.impl.aaa;

import de.mhus.osgi.sop.api.aaa.AccountGuest;
import de.mhus.osgi.sop.impl.AaaContextImpl;

public class GuestContext extends AaaContextImpl {

	public GuestContext() {
		super(new AccountGuest());
		adminMode = false;
	}

}
