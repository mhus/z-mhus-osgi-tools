package de.mhus.osgi.services.cache;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.spi.loaderwriter.BulkCacheLoadingException;
import org.ehcache.spi.loaderwriter.BulkCacheWritingException;
import org.ehcache.spi.loaderwriter.CacheLoadingException;
import org.ehcache.spi.loaderwriter.CacheWritingException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;

import de.mhus.lib.core.MLog;
import de.mhus.osgi.api.cache.CloseableCache;
import de.mhus.osgi.api.services.MOsgi;

public class CacheWrapper<K,V> extends MLog implements CloseableCache<K, V> {

    private Cache<K,V> instance;
    private CacheManager manager;
    private String name;
    private StatisticsService statisticsService;
    private BundleContext bundleContext;
    @SuppressWarnings("rawtypes")
    private ServiceRegistration<CloseableCache> serviceRegistration;

    public CacheWrapper(CacheManager cacheManager, Cache<K, V> cache, String name, BundleContext bundleContext, StatisticsService statisticsService) {
        log().d("open",name);
        this.manager = cacheManager;
        this.instance = cache;
        this.name = name;
        this.statisticsService = statisticsService;
        this.bundleContext = bundleContext;
        
        serviceRegistration = bundleContext.registerService(CloseableCache.class, this, MOsgi.createProperties("name",name));
        try {
            bundleContext.addServiceListener(ev -> serviceListener(ev), MOsgi.filterObjectClass(CloseableCache.class));
        } catch (InvalidSyntaxException e) {
            log().e(name,e);
        }
    }

    private Object serviceListener(ServiceEvent ev) {
        if (ev.getServiceReference().equals(serviceRegistration.getReference()) && ev.getType() == ServiceEvent.UNREGISTERING )
            log().d("unregister",name);
            try {
                serviceRegistration = null;
                close();
            } catch (IOException e) {
                log().e(name,e);
                
            }
        return null;
    }

    public CacheStatistics getCacheStatistics() {
        return statisticsService.getCacheStatistics(name);
    }
    
    @Override
    public CacheManager getCacheManager() {
        return manager;
    }
    
    @Override
    public void forEach(Consumer<? super Entry<K, V>> action) {
        instance.forEach(action);
    }

    @Override
    public V get(K key) throws CacheLoadingException {
        return instance.get(key);
    }

    @Override
    public void put(K key, V value) throws CacheWritingException {
        instance.put(key, value);
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        return instance.spliterator();
    }

    @Override
    public boolean containsKey(K key) {
        return instance.containsKey(key);
    }

    @Override
    public void remove(K key) throws CacheWritingException {
        instance.remove(key);
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) throws BulkCacheLoadingException {
        return instance.getAll(keys);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> entries) throws BulkCacheWritingException {
        instance.putAll(entries);
    }

    @Override
    public void removeAll(Set<? extends K> keys) throws BulkCacheWritingException {
        instance.removeAll(keys);
    }

    @Override
    public void clear() {
        instance.clear();
    }

    @Override
    public V putIfAbsent(K key, V value) throws CacheLoadingException, CacheWritingException {
        return instance.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(K key, V value) throws CacheWritingException {
        return instance.remove(key, value);
    }

    @Override
    public V replace(K key, V value) throws CacheLoadingException, CacheWritingException {
        return instance.replace(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) throws CacheLoadingException, CacheWritingException {
        return instance.replace(key, oldValue, newValue);
    }

    @Override
    public CacheRuntimeConfiguration<K, V> getRuntimeConfiguration() {
        return instance.getRuntimeConfiguration();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return instance.iterator();
    }

    @Override
    public void close() throws IOException {
        log().d("close",name);
        if (serviceRegistration != null) {
            @SuppressWarnings("rawtypes")
            ServiceRegistration<CloseableCache> sr = serviceRegistration; // need to set it null before unregister for the listener
            serviceRegistration = null;
            sr.unregister();
        }
        if (manager != null) {
            manager.close();
            manager = null;
        }
    }

    @Override
    public Bundle getBundle() {
        return bundleContext.getBundle();
    }

}
