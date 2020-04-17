package de.mhus.osgi.services.cache;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;

import java.util.WeakHashMap;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.MLog;
import de.mhus.osgi.api.cache.CacheService;
import de.mhus.osgi.api.cache.CloseableCache;

@Component
public class CacheServiceImpl extends MLog implements CacheService {

    private CacheManagerBuilder<CacheManager> cacheBuilder;
    private WeakHashMap<String, CacheWrapper<?,?>> register = new  WeakHashMap<>();

    @Override
    public CacheManagerBuilder<CacheManager> getCacheBuilder() {
        if (cacheBuilder == null)
            cacheBuilder = CacheManagerBuilder.newCacheManagerBuilder();
        return cacheBuilder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> CloseableCache<K, V> createCache(Class<?> owner, String name, Class<K> keyType,
            Class<V> valueType, Builder<? extends ResourcePools> resourcePoolsBuilder) {

        name = owner.getCanonicalName() + "." + name;
        CacheWrapper<?, ?> weak = register.get(name);
        if (weak != null) return (CloseableCache<K, V>) weak;

        CacheManager cacheManager = getCacheBuilder().withCache(name,
                newCacheConfigurationBuilder(keyType, valueType, resourcePoolsBuilder))
              .build(true);

        Cache<K, V> cache = cacheManager.getCache(name, keyType, valueType);
        CacheWrapper<K,V> wrapper = new CacheWrapper<>(cacheManager, cache, name, register);
        return wrapper;
    }
    
    @Override
    public String[] getCaches() {
        return register.keySet().toArray(new String[0]);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> CloseableCache<K, V> getCache(String name) {
        return (CloseableCache<K, V>) register.get(name);
    }


}
