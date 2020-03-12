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
package de.mhus.osgi.api.aaa;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ContextCachedItem {

    public ContextCachedItem() {}

    public ContextCachedItem(Object obj) {
        setObject(obj);
    }

    public ContextCachedItem(long ttl) {
        this.ttl = ttl;
    }

    public ContextCachedItem(long ttl, Object obj) {
        this.ttl = ttl;
        setObject(obj);
    }

    private long ttl = 1000 * 60;
    private long created = System.currentTimeMillis();

    public boolean bool;
    public int integer;
    private Object object;
    private Bundle bundle;
    private long modified;

    public boolean isValid() {
        if (System.currentTimeMillis() - created > ttl) return false;

        if (bundle != null && modified != bundle.getLastModified()) return false;

        return true;
    }

    public void invalidate() {
        ttl = 0;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        if (object != null) {
            this.bundle = FrameworkUtil.getBundle(object.getClass());
            this.modified = bundle.getLastModified();
        }
        this.object = object;
    }
}
