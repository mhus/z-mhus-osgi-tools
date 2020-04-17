package de.mhus.osgi.api.cache;

import org.ehcache.CacheManager;
import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheManagerBuilder;

public interface CacheService {

    CacheManagerBuilder<CacheManager> getCacheBuilder();
    
    <K,V> CloseableCache<K, V> createCache(Class<?> owner, String name, Class<K> keyType, Class<V> valueType, Builder<? extends ResourcePools> resourcePoolsBuilder );
    
}
