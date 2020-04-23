package de.mhus.osgi.api.cache;

import java.util.List;
import java.util.function.Consumer;

import org.ehcache.CacheManager;
import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.osgi.framework.BundleContext;

public interface LocalCacheService {

    // ccb -> ccb.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(5)))

    CacheManagerBuilder<CacheManager> getCacheBuilder();

    default <K,V> LocalCache<K, V> createCache(BundleContext ownerContext, String name, Class<K> keyType, Class<V> valueType, int heapSize) {
        return createCache(ownerContext, name, keyType, valueType, ResourcePoolsBuilder.heap(heapSize), null);
    }

    default <K,V> LocalCache<K, V> createCache(BundleContext ownerContext, String name, Class<K> keyType, Class<V> valueType, Builder<? extends ResourcePools> resourcePoolsBuilder) {
        return createCache(ownerContext, name, keyType, valueType, resourcePoolsBuilder, null);
    }

    <K,V> LocalCache<K, V> createCache(BundleContext ownerContext, String name, Class<K> keyType, Class<V> valueType, Builder<? extends ResourcePools> resourcePoolsBuilder, Consumer<CacheConfigurationBuilder<K,V>> configurator );

    List<String> getCacheNames();

    <K,V> LocalCache<K, V> getCache(String name);

}
