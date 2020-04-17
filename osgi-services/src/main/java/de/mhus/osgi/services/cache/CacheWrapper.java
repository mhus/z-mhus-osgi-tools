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

import de.mhus.osgi.api.cache.CloseableCache;

public class CacheWrapper<K,V> implements CloseableCache<K, V> {

    private Cache<K,V> instance;
    private CacheManager manager;
    private String name;
    private Map<String, CacheWrapper<?, ?>> register;
    private StatisticsService statisticsService;

    public CacheWrapper(CacheManager cacheManager, Cache<K, V> cache, String name, Map<String, CacheWrapper<?,?>> register, StatisticsService statisticsService) {
        this.manager = cacheManager;
        this.instance = cache;
        this.name = name;
        this.register = register;
        this.statisticsService = statisticsService;
        register.put(name, this);
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
        if (manager != null) {
            manager.close();
            register.remove(name);
        }
        manager = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
}
