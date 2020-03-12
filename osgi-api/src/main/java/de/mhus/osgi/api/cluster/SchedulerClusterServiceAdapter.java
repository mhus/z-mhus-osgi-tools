package de.mhus.osgi.api.cluster;

import de.mhus.lib.core.schedule.TimerTaskInterceptor;
import de.mhus.osgi.api.scheduler.SchedulerServiceAdapter;

public abstract class SchedulerClusterServiceAdapter extends SchedulerServiceAdapter {

    private TimerTaskInterceptor interceptor;
    private boolean service = true;

    @Override
    public synchronized TimerTaskInterceptor getInterceptor() {
        if (interceptor == null) {
            interceptor = new TimerTaskClusterInterceptor(service);
        }
        return interceptor;
    }

    public boolean isService() {
        return service;
    }

    protected void setStack(boolean service) {
        this.service = service;
    }
}
