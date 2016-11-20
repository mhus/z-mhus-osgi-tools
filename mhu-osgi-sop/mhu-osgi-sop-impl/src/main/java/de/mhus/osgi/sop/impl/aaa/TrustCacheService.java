package de.mhus.osgi.sop.impl.aaa;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.karaf.services.CacheControlIfc;

@Component
public class TrustCacheService implements CacheControlIfc {

	@Override
	public long getSize() {
		return AccessApiImpl.instance.trustCache.size();
	}

	@Override
	public String getName() {
		return "de.mhus.osgi.sop.impl.aaa.trustCache";
	}

	@Override
	public void clear() {
		AccessApiImpl.instance.trustCache.clear();
	}

}
