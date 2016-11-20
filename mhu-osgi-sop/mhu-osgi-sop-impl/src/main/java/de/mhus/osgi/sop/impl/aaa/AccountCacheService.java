package de.mhus.osgi.sop.impl.aaa;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.karaf.services.CacheControlIfc;

@Component
public class AccountCacheService implements CacheControlIfc {

	@Override
	public long getSize() {
		return AccessApiImpl.instance.accountCache.size();
	}

	@Override
	public String getName() {
		return "de.mhus.osgi.sop.impl.aaa.accountCache";
	}

	@Override
	public void clear() {
		AccessApiImpl.instance.accountCache.clear();
	}

}
