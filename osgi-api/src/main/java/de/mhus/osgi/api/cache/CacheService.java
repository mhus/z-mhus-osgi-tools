package de.mhus.osgi.api.cache;

import java.util.List;

import org.ehcache.CacheManager;
import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.osgi.framework.BundleContext;

public interface CacheService {

    CacheManagerBuilder<CacheManager> getCacheBuilder();
    
    <K,V> CloseableCache<K, V> createCache(BundleContext ownerContext, String name, Class<K> keyType, Class<V> valueType, Builder<? extends ResourcePools> resourcePoolsBuilder );

    List<String> getCacheNames();

    <K,V> CloseableCache<K, V> getCache(String name);
    
}
