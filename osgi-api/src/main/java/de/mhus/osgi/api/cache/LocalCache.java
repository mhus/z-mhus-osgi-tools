package de.mhus.osgi.api.cache;

import java.io.Closeable;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.osgi.framework.Bundle;

public interface LocalCache<K, V> extends Cache<K, V>, Closeable {

    CacheManager getCacheManager();

    Bundle getBundle();
}
