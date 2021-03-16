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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

import org.ehcache.config.CacheRuntimeConfiguration;
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

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.cache.LocalCache;
import de.mhus.lib.core.cache.LocalCacheStatistics;
import de.mhus.osgi.api.MOsgi;

public class LocalCacheWrapper<K, V> implements LocalCache<K, V> {

    private org.ehcache.Cache<K, V> instance;
    private String name;
    private BundleContext bundleContext;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<LocalCache> serviceRegistration;
    private LocalCacheServiceImpl service;

    public LocalCacheWrapper(
            LocalCacheServiceImpl service,
            org.ehcache.Cache<K, V> cache,
            String name,
            BundleContext bundleContext) {
        MApi.dirtyLogDebug("LocalCacheWrapper","open", name);
        this.service = service; 
        this.instance = cache;
        this.name = name;
        this.bundleContext = bundleContext;

        serviceRegistration =
                bundleContext.registerService(
                        LocalCache.class, this, MOsgi.createProperties("name", name));
        try {
            // add listener to services. If my service is shut down, stop cache
            bundleContext.addServiceListener(
                    ev -> serviceListener(ev), MOsgi.filterObjectClass(LocalCache.class));
        } catch (InvalidSyntaxException e) {
            MApi.dirtyLogDebug("LocalCacheWrapper",name, e);
        }
    }

    public CacheStatistics getEhCacheStatistics() {
        return service.getStatisticsService().getCacheStatistics(name);
    }
    
    private void serviceListener(ServiceEvent ev) {
        try {
            if (ev == null || ev.getServiceReference() == null || serviceRegistration == null) return;
            if (ev.getServiceReference().equals(serviceRegistration.getReference())
                && ev.getType() == ServiceEvent.UNREGISTERING) {
                MApi.dirtyLogDebug("LocalCacheWrapper","unregister", name);
                serviceRegistration = null;
                close();
            }
        } catch (Throwable e) {
            MApi.dirtyLogDebug("LocalCacheWrapper",name, e);
        }
    }

    public LocalCacheStatistics getCacheStatistics() {
        return new WrapperCacheStatistics(service.getStatisticsService().getCacheStatistics(name));
    }

    @Override
    public CacheManager getCacheManager() {
        return service.getCacheManagerWrapper();
    }

    @Override
    public void forEach(Consumer<? super Entry<K, V>> action) {
        instance.forEach(e -> action.accept( new EntryWrapper<K, V>(e) ) );
    }

    private static class EntryWrapper<K,V> implements javax.cache.Cache.Entry<K,V> {
        
        org.ehcache.Cache.Entry<K,V> entry;
        
        public EntryWrapper(org.ehcache.Cache.Entry<K, V> e) {
            this.entry = e;
        }
        
        @Override
        public K getKey() {
            return entry.getKey();
        }

        @Override
        public V getValue() {
            return entry.getValue();
        }

        @Override
        public <T> T unwrap(Class<T> clazz) {
            return null;
        }
        
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
//        return instance.spliterator();
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        return instance.containsKey(key);
    }

    @Override
    public boolean remove(K key) throws CacheWritingException {
        if (!instance.containsKey(key)) return false;
        instance.remove(key);
        return true;
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
    public boolean putIfAbsent(K key, V value) throws CacheLoadingException, CacheWritingException {
        if (instance.containsKey(key)) return false;
        instance.putIfAbsent(key, value);
        return true;
    }

    @Override
    public boolean remove(K key, V value) throws CacheWritingException {
        return instance.remove(key, value);
    }

    @Override
    public boolean replace(K key, V value) throws CacheLoadingException, CacheWritingException {
        return instance.replace(key, value) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
            throws CacheLoadingException, CacheWritingException {
        return instance.replace(key, oldValue, newValue);
    }

    public CacheRuntimeConfiguration<K, V> getRuntimeConfiguration() {
        return instance.getRuntimeConfiguration();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new InteratorWrapper<K,V>(instance.iterator());
    }

    private static class InteratorWrapper<K, V> implements Iterator<Entry<K, V>> {

        private Iterator<org.ehcache.Cache.Entry<K, V>> iterator;

        public InteratorWrapper(Iterator<org.ehcache.Cache.Entry<K, V>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return new EntryWrapper<K,V>(iterator.next());
        }
        
    }
    
    @Override
    public void close() {
        MApi.dirtyLogDebug("LocalCacheWrapper","close", name);
        if (serviceRegistration != null) {
            @SuppressWarnings("rawtypes")
            ServiceRegistration<LocalCache> sr =
                    serviceRegistration; // need to set it null before unregister for the listener
            serviceRegistration = null;
            sr.unregister();
        }
        service.getCacheManager().removeCache(name);
    }

    public Bundle getBundle() {
        return bundleContext.getBundle();
    }

    public LocalCacheServiceImpl getService() {
        return service;
    }

    @Override
    public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
        // TODO Auto-generated method stub
    }

    @Override
    public V getAndPut(K key, V value) {
        V cur = instance.get(key);
        instance.put(key, value);
        return cur;
    }

    @Override
    public V getAndRemove(K key) {
        return null;
    }

    @Override
    public V getAndReplace(K key, V value) {
        V cur = instance.get(key);
        instance.replace(key, value);
        return cur;
    }

    @Override
    public void removeAll() {
        instance.clear();
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
            throws EntryProcessorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor,
            Object... arguments) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isClosed() {
        return serviceRegistration == null;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        // TODO Auto-generated method stub
        
    }
}
