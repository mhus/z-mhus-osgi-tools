/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.services.aaa;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.security.AaaContext;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.osgi.api.aaa.ContextCachedItem;
import de.mhus.osgi.api.services.AbstractCacheControl;
import de.mhus.osgi.api.services.CacheControlIfc;

@Component(service = CacheControlIfc.class)
public class CoreContextCacheService extends AbstractCacheControl {

    private SoftHashMap<String, ContextCachedItem> cache =
            new SoftHashMap<String, ContextCachedItem>();
    private static CoreContextCacheService instance;

    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }

    @Override
    public long getSize() {
        synchronized (cache) {
            return cache.size();
        }
    }

    @Override
    public void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }

    private void privSet(AaaContext context, String key, long ttl, Object value) {
        if (!enabled) return;
        synchronized (cache) {
            cache.put(context.getAccountId() + "|" + key, new ContextCachedItem(ttl, value));
        }
    }

    private Object privGet(AaaContext context, String key) {
        synchronized (cache) {
            ContextCachedItem cont = cache.get(context.getAccountId() + "|" + key);
            if (cont == null) return null;
            if (!cont.isValid()) {
                cache.remove(context.getAccountId() + "|" + key);
                return null;
            }
            return cont.getObject();
        }
    }

    static void set(AaaContext context, String key, long ttl, Object value) {
        if (instance == null) return;
        instance.privSet(context, key, ttl, value);
    }

    @SuppressWarnings("unchecked")
    static <T> T get(AaaContext context, String key) {
        if (instance == null) return null;
        return (T) instance.privGet(context, key);
    }
}
