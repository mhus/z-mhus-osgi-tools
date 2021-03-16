package de.mhus.osgi.services.cache;

import java.net.URI;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

import de.mhus.lib.core.MLog;

public class CacheManagerWrapper extends MLog implements CacheManager {

    private LocalCacheServiceImpl service;

    public CacheManagerWrapper(LocalCacheServiceImpl service) {
        this.service = service;
    }

    @Override
    public CachingProvider getCachingProvider() {
        return null;
    }

    @Override
    public URI getURI() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration)
            throws IllegalArgumentException {
        return service.getCache(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return service.getCache(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return service.getCache(cacheName);
    }

    @Override
    public Iterable<String> getCacheNames() {
        return service.getCacheNames();
    }

    @Override
    public void destroyCache(String cacheName) {
        log().w("destroy",cacheName);
        service.getCacheManager().removeCache(cacheName);
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {
        
    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {
        
    }

    @Override
    public void close() {
        
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }

}
