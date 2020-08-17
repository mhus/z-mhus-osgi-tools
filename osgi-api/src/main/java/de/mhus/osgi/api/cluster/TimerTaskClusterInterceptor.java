/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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
package de.mhus.osgi.api.cluster;

import de.mhus.lib.core.M;
import de.mhus.lib.core.concurrent.Lock;
import de.mhus.lib.core.concurrent.LockWithExtend;
import de.mhus.lib.core.operation.DefaultTaskContext;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.TimerTaskInterceptor;
import de.mhus.lib.core.service.ClusterApi;

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
