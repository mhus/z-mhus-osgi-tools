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
