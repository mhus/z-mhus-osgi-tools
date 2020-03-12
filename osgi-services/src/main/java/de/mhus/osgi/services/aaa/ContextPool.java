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

import de.mhus.lib.core.MLog;

public class ContextPool extends MLog {

    private static ContextPool instance;
    private ThreadLocal<AaaContextImpl> pool = new ThreadLocal<>();

    private ContextPool() {
        log().d("Create Context Pool");
    }

    public static synchronized ContextPool getInstance() {
        if (instance == null) instance = new ContextPool();
        return instance;
    }

    public AaaContextImpl getCurrent() {
        synchronized (pool) {
            return pool.get();
        }
    }

    public void set(AaaContextImpl context, boolean forward) {
        log().d("set", context, forward);
        synchronized (pool) {
            AaaContextImpl parent = pool.get();
            if (context != null) {
                if (forward) context.setParent(parent);
                pool.set(context);
            } else {
                pool.remove();
            }
        }
    }
}
