package de.mhus.osgi.services.cache;

import java.util.List;
import java.util.function.Consumer;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Builder;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.core.statistics.DefaultStatisticsService;
import org.ehcache.impl.config.copy.DefaultCopierConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.cache.LocalCacheService;
import de.mhus.osgi.api.cache.LocalCache;
import de.mhus.osgi.api.services.MOsgi;

@Component
public class LocalCacheServiceImpl extends MLog implements LocalCacheService {

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
    public <K, V> LocalCache<K, V> createCache(BundleContext ownerContext, String name, Class<K> keyType,
            Class<V> valueType, Builder<? extends ResourcePools> resourcePoolsBuilder, Consumer<CacheConfigurationBuilder<K,V>> configurator ) {

        name = ownerContext.getBundle().getSymbolicName() + ":" + ownerContext.getBundle().getBundleId() + "/" + name;
        LocalCache<Object, Object> existing = getCache(name);
        if (existing != null && existing.getBundle().getBundleId() == ownerContext.getBundle().getBundleId())
            return (LocalCache<K, V>) existing;

        if (statisticsService == null)
            statisticsService = new DefaultStatisticsService();

        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .using(statisticsService)
                .build(false);
        cacheManager.init();

        
        CacheConfigurationBuilder<K, V> ccb = CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType, resourcePoolsBuilder);
        
        if (resourcePoolsBuilder == null)
            throw new NullPointerException("resourcePoolsBuilder is null");
        
        if (configurator != null) {
            configurator.accept(ccb);
        } else {
            // default configuration
            @SuppressWarnings("rawtypes")
            DefaultCopierConfiguration<String> copierConfigurationKey = new DefaultCopierConfiguration(
                    NoneCopier.class, DefaultCopierConfiguration.Type.KEY);
            @SuppressWarnings("rawtypes")
            DefaultCopierConfiguration<String> copierConfigurationValue = new DefaultCopierConfiguration(
                    NoneCopier.class, DefaultCopierConfiguration.Type.VALUE);
            ccb
                .withService(copierConfigurationKey)
                .withService(copierConfigurationValue);
        }
        
        Cache<K, V> cache = cacheManager.createCache(name, ccb.build());

        LocalCacheWrapper<K,V> wrapper = new LocalCacheWrapper<>(cacheManager, cache, name, ownerContext, statisticsService);
        return wrapper;
    }
    
    @Override
    public List<String> getCacheNames() {
        return MOsgi.collectStringProperty( MOsgi.getServiceRefs(LocalCache.class, null) , "name");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <K, V> LocalCache<K, V> getCache(String name) {
        try {
            return MOsgi.getService(LocalCache.class, MOsgi.filterValue("name",name));
        } catch (NotFoundException e) {
            log().t("not found",name);
            return null;
        }
    }


}
