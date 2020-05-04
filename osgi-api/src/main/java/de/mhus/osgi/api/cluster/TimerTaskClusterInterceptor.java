package de.mhus.osgi.api.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.base.service.ClusterApi;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.concurrent.LockWithExtend;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.TimerTaskInterceptor;

public class TimerTaskClusterInterceptor implements TimerTaskInterceptor {

    private String name;
    private Lock mutex;
    private boolean service;

    public TimerTaskClusterInterceptor() {
        service = true;
    }

    public TimerTaskClusterInterceptor(boolean service) {
        this.service = service;
    }

    public TimerTaskClusterInterceptor(String name, boolean service) {
        this.name = name;
        this.service = service;
    }

    @Override
    public void initialize(SchedulerJob job) {
        if (name == null) name = job.getName();
    }

    @Override
    public boolean beforeExecution(SchedulerJob job, DefaultTaskContext context, boolean forced) {
        if (mutex != null) {
            mutex.close();
            mutex = null;
        }
        if (!ClusterApi.CFG_ENABLED.value()) return true;
        ClusterApi api = M.l(ClusterApi.class);
        @SuppressWarnings("resource")
        Lock lock = service ? api.getServiceLock(name) : api.getLock(name);
        if (lock.isLocked()) return false;
        mutex = lock.lock();
        return true;
    }

    @Override
    public void afterExecution(SchedulerJob job, DefaultTaskContext context) {
        if (mutex != null) {
            if (mutex instanceof LockWithExtend)
                // extends lock 10 sec. before next execution
                ((LockWithExtend) mutex)
                        .unlock(job.getNextExecutionTime() - System.currentTimeMillis() - 10000);
            else mutex.unlock();
            mutex = null;
        }
    }

    @Override
    public void onError(SchedulerJob job, DefaultTaskContext context, Throwable e) {}

    public Lock getLock() {
        return mutex;
    }
}
