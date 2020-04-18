package de.mhus.osgi.services.cache;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;

import java.util.List;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.core.statistics.DefaultStatisticsService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.cache.CacheService;
import de.mhus.osgi.api.cache.CloseableCache;
import de.mhus.osgi.api.services.MOsgi;

@Component
public class CacheServiceImpl extends MLog implements CacheService {

    private CacheManagerBuilder<CacheManager> cacheBuilder;
    private DefaultStatisticsService statisticsService;

    @Override
    public CacheManagerBuilder<CacheManager> getCacheBuilder() {
        if (cacheBuilder == null)
            cacheBuilder = CacheManagerBuilder.newCacheManagerBuilder();
        return cacheBuilder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> CloseableCache<K, V> createCache(BundleContext ownerContext, String name, Class<K> keyType,
            Class<V> valueType, Builder<? extends ResourcePools> resourcePoolsBuilder) {

        name = ownerContext.getBundle().getSymbolicName() + ":" + ownerContext.getBundle().getBundleId() + "/" + name;
        CloseableCache<Object, Object> existing = getCache(name);
        if (existing != null && existing.getBundle().getBundleId() == ownerContext.getBundle().getBundleId())
            return (CloseableCache<K, V>) existing;

        if (statisticsService == null)
            statisticsService = new DefaultStatisticsService();
        
        CacheManager cacheManager = getCacheBuilder().withCache(name,
                newCacheConfigurationBuilder(keyType, valueType, resourcePoolsBuilder))
                .using(statisticsService)
                .build(true);

        Cache<K, V> cache = cacheManager.getCache(name, keyType, valueType);
        CacheWrapper<K,V> wrapper = new CacheWrapper<>(cacheManager, cache, name, ownerContext, statisticsService);
        return wrapper;
    }
    
    @Override
    public List<String> getCacheNames() {
        return MOsgi.collectStringProperty( MOsgi.getServiceRefs(CloseableCache.class, null) , "name");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> CloseableCache<K, V> getCache(String name) {
        try {
            return MOsgi.getService(CloseableCache.class, MOsgi.filterValue("name",name));
        } catch (NotFoundException e) {
            log().t("not found",name);
            return null;
        }
    }


}
