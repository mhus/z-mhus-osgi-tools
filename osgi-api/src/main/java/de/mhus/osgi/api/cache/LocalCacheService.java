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

    default <K, V> LocalCache<K, V> createCache(
            BundleContext ownerContext,
            String name,
            Class<K> keyType,
            Class<V> valueType,
            int heapSize) {
        return createCache(
                ownerContext, name, keyType, valueType, ResourcePoolsBuilder.heap(heapSize), null);
    }

    default <K, V> LocalCache<K, V> createCache(
            BundleContext ownerContext,
            String name,
            Class<K> keyType,
            Class<V> valueType,
            Builder<? extends ResourcePools> resourcePoolsBuilder) {
        return createCache(ownerContext, name, keyType, valueType, resourcePoolsBuilder, null);
    }

    <K, V> LocalCache<K, V> createCache(
            BundleContext ownerContext,
            String name,
            Class<K> keyType,
            Class<V> valueType,
            Builder<? extends ResourcePools> resourcePoolsBuilder,
            Consumer<CacheConfigurationBuilder<K, V>> configurator);

    List<String> getCacheNames();

    <K, V> LocalCache<K, V> getCache(String name);
}
