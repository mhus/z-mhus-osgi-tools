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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;

import de.mhus.lib.basics.Clearable;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.aaa.AccessApi;
import de.mhus.lib.core.cache.ICache;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.util.SoftTimeoutMap;

public class OsgiCacheManager implements CacheManager, Clearable {

    private static CfgLong CFG_TTL =
            new CfgLong(
                    AccessApi.class, "authorizationCacheTTL", MPeriod.MINUTE_IN_MILLISECOUNDS * 30);
//    private static CfgInt CFG_SIZE = new CfgInt(AccessApi.class, "authorizationCacheSize", 100000);
    private static CfgBoolean CFG_ENABLED =
            new CfgBoolean(AccessApi.class, "authorizationCacheEnabled", true);

    @SuppressWarnings("rawtypes")
    private Map<String, Cache> caches = Collections.synchronizedMap(new HashMap<>());

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {

        if (!CFG_ENABLED.value()) return new DummyCache();
        Cache<K, V> inst = caches.get(name);
        if (inst != null) return (Cache<K, V>) inst;

//        ICacheService service = M.l(ICacheService.class);
//        if (service == null) return null;
//        ICache<Object, Object> c =
//                service.createCache(
//                        this,
//                        name,
//                        Object.class,
//                        Object.class,
//                        new CacheConfig().setHeapSize(CFG_SIZE.value()).setTTL(CFG_TTL.value()));

//        inst = new CacheWrapper<>(c);
      inst = new LocalCache<>();
        caches.put(name, inst);
        return inst;
    }
    
    @Override
    public void clear() {
        caches.forEach( (k,v) -> v.clear()  );
    }

    private static class LocalCache<K, V> implements Cache<K, V> {

        SoftTimeoutMap<K, V> cache = new SoftTimeoutMap<>(CFG_TTL.value());

        @Override
        public void clear() throws CacheException {
            cache.clear();
        }

        @Override
        public V get(K arg0) throws CacheException {
            return cache.get(arg0);
        }

        @Override
        public Set<K> keys() {
            return cache.keySet();
        }

        @Override
        public V put(K arg0, V arg1) throws CacheException {
            return cache.put(arg0, arg1);
        }

        @Override
        public V remove(K arg0) throws CacheException {
            return cache.remove(arg0);
        }

        @Override
        public int size() {
            return cache.size();
        }

        @Override
        public Collection<V> values() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    @SuppressWarnings("unused")
    private static class CacheWrapper<K, V> implements Cache<K, V> {

        private ICache<Object, Object> inst;

        public CacheWrapper(ICache<Object, Object> inst) {
            this.inst = inst;
        }

        @SuppressWarnings("unchecked")
        @Override
        public V get(Object key) {
            return (V) inst.get(key);
        }

        @Override
        public V put(K key, V value) {
            V val = get(key);
            inst.put(key, value);
            return val;
        }

        @Override
        public V remove(Object key) {
            V val = get(key);
            inst.remove(key);
            return val;
        }

        @Override
        public void clear() {
            inst.clear();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<K> keys() {
            HashSet<K> ret = new HashSet<>();
            inst.forEach(e -> ret.add((K) e.getKey()));
            return ret;
        }

        @Override
        public int size() {
            return 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<V> values() {
            ArrayList<V> ret = new ArrayList<>();
            inst.forEach(e -> ret.add((V) e.getValue()));
            return ret;
        }
    }

    private static class DummyCache<K, V> implements Cache<K, V> {

        @Override
        public void clear() throws CacheException {}

        @Override
        public V get(K arg0) throws CacheException {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Set<K> keys() {
            return (Set<K>) MCollection.EMPTY_SET;
        }

        @Override
        public V put(K arg0, V arg1) throws CacheException {
            return null;
        }

        @Override
        public V remove(K arg0) throws CacheException {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<V> values() {
            return (Collection<V>) MCollection.EMPTY_LIST;
        }
    }
}
