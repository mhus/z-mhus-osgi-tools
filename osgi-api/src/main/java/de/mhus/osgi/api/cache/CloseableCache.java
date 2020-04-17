package de.mhus.osgi.api.cache;

import java.io.Closeable;

import org.ehcache.Cache;
import org.ehcache.CacheManager;

public interface CloseableCache<K,V> extends Cache<K,V>, Closeable {

    CacheManager getCacheManager();

}
