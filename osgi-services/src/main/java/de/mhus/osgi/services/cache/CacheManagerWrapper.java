/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(
            String cacheName, C configuration) throws IllegalArgumentException {
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
        log().w("destroy", cacheName);
        service.getCacheManager().removeCache(cacheName);
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {}

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {}

    @Override
    public void close() {}

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }
}
