package de.mhus.osgi.sop.impl.aaa;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.karaf.services.AbstractCacheControl;
import de.mhus.lib.karaf.services.CacheControlIfc;

@Component(provide=CacheControlIfc.class)
public class AccountCacheService extends AbstractCacheControl {

	{
		supportDisable = false;
	}

	@Override
	public long getSize() {
		return AccessApiImpl.instance.accountCache.size();
	}

	@Override
	public void clear() {
		AccessApiImpl.instance.accountCache.clear();
	}

}
