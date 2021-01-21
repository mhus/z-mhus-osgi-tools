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
package de.mhus.osgi.api.services;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;

public abstract class BundleStarter extends MLog implements Runnable {

    private long timeout = MPeriod.MINUTE_IN_MILLISECOUNDS;
    private boolean exitOnTimeout = true;
    private boolean retry = false;

    public long getTimeout() {
        return timeout;
    }

    protected void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean exitOnTimeout() {
        return exitOnTimeout;
    }

    protected void setExitOnTimeout(boolean exitOnTimeout) {
        this.exitOnTimeout = exitOnTimeout;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }
}
