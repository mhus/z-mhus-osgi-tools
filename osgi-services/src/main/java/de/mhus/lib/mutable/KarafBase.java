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
package de.mhus.lib.mutable;

import static org.ehcache.config.units.MemoryUnit.MB;

import java.io.Serializable;

import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.lang.Base;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.system.DefaultBase;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.cache.CacheService;
import de.mhus.osgi.api.cache.CloseableCache;
import de.mhus.osgi.api.services.MOsgi;

public class KarafBase extends DefaultBase {


    private CloseableCache<String, Container> apiCache;

    public KarafBase(Base parent) {
        super(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, D extends T> T lookup(Class<T> ifc, Class<D> def) {

        if (ifc == null) return null;

        if (def == null && ifc.isInterface()) { // only interfaces can be OSGi services

            if (apiCache == null) {
                try {
                    CacheService cacheService = MOsgi.getService(CacheService.class);
                    apiCache = cacheService.createCache(
                            FrameworkUtil.getBundle(KarafBase.class).getBundleContext(),
                            "baseApi", 
                            String.class, Container.class, 
                            ResourcePoolsBuilder.heap(100).offheap(1, MB) 
                            );
                } catch (NotFoundException e) {}
            }

            Container cached = null;
            if (apiCache != null) {
                cached = apiCache.get(ifc.getCanonicalName());
                if (cached != null) {
                    if (cached.bundle.getState() != Bundle.ACTIVE
                            || cached.modified != cached.bundle.getLastModified()) {
                        apiCache.remove(cached.ifc.getCanonicalName());
                        cached = null;
                    }
                }
            }
            
            if (cached == null) {
                Bundle bundle = FrameworkUtil.getBundle(KarafBase.class);
                if (bundle != null) {
                    BundleContext context = bundle.getBundleContext();
                    if (context != null) {
                        ServiceReference<? extends T> ref = context.getServiceReference(ifc);
                        if (ref != null) {
                            if (ref.getBundle().getState() != Bundle.ACTIVE) {
                                MLogUtil.log()
                                        .d(
                                                "KarafBase",
                                                "found in bundle but not jet active",
                                                ifc,
                                                bundle.getSymbolicName());
                                return null;
                            }
                            T obj = null;
                            try {
                                obj = ref.getBundle().getBundleContext().getService(ref);
                                //								obj = context.getService(ref);
                            } catch (Throwable t) {
                            }
                            if (obj != null) {
                                MLogUtil.log().d("KarafBase", "loaded from OSGi", ifc);
                                cached = new Container();
                                cached.bundle = ref.getBundle();
                                cached.api = obj;
                                cached.ifc = ifc;
                                cached.modified = cached.bundle.getLastModified();
                                apiCache.put(ifc.getCanonicalName(), cached);
                            }
                        }
                    }
                }
            }
            if (cached != null) return (T) cached.api;
        }
        return super.lookup(ifc, def);
    }

    public static class Container implements Serializable {

        private static final long serialVersionUID = 1L;
        public long modified;
        public Class<?> ifc;
        public Object api;
        public Bundle bundle;
    }

}
