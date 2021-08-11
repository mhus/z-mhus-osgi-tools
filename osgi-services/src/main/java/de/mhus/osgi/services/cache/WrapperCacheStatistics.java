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

import org.ehcache.core.statistics.CacheStatistics;

import de.mhus.lib.core.cache.ICacheStatistics;

public class WrapperCacheStatistics implements ICacheStatistics {

    private CacheStatistics instance;

    public WrapperCacheStatistics(CacheStatistics instance) {
        this.instance = instance;
    }

    @Override
    public void clear() {
        instance.clear();
    }

    @Override
    public long getCacheHits() {
        return instance.getCacheHits();
    }

    @Override
    public float getCacheHitPercentage() {
        return instance.getCacheHitPercentage();
    }

    @Override
    public long getCacheMisses() {
        return instance.getCacheMisses();
    }

    @Override
    public float getCacheMissPercentage() {
        return instance.getCacheMissPercentage();
    }

    @Override
    public long getCacheGets() {
        return instance.getCacheGets();
    }

    @Override
    public long getCachePuts() {
        return instance.getCachePuts();
    }

    @Override
    public long getCacheRemovals() {
        return instance.getCacheRemovals();
    }

    @Override
    public long getCacheEvictions() {
        return instance.getCacheEvictions();
    }

    @Override
    public float getAverageGetTime() {
        return 0;
    }

    @Override
    public float getAveragePutTime() {
        return 0;
    }

    @Override
    public float getAverageRemoveTime() {
        return 0;
    }

    @Override
    public long getCacheSize() {
        return instance.getTierStatistics().get("OnHeap").getMappings();
    }

    @Override
    public long getOccupiedByteSize() {
        return instance.getTierStatistics().get("OnHeap").getOccupiedByteSize();
    }
}
