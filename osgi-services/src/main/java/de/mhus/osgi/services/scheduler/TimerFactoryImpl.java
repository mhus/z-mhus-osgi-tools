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
package de.mhus.osgi.services.scheduler;

import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.WeakHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.basics.Named;
import de.mhus.lib.core.ITimerTask;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.schedule.MutableSchedulerJob;
import de.mhus.lib.core.schedule.Scheduler;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.SchedulerTimer;
import de.mhus.lib.core.schedule.TimerTaskInterceptor;
import de.mhus.lib.core.service.TimerFactory;
import de.mhus.lib.core.service.TimerIfc;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.MOsgi.Service;
import de.mhus.osgi.api.scheduler.SchedulerService;
import de.mhus.osgi.api.util.AbstractServiceTracker;

@Component(
        service = TimerFactory.class,
        immediate = true,
        name = "de.mhus.lib.karaf.services.TimerFactoryImpl")
public class TimerFactoryImpl extends MLog implements TimerFactory {

    protected static Log log = Log.getLog(TimerFactoryImpl.class);
    private SchedulerTimer myTimer = new SchedulerTimer("de.mhus.lib.karaf.Scheduler");
    private AbstractServiceTracker<SchedulerService> tracker;
    private WeakHashMap<SchedulerService, SchedulerJob> services = new WeakHashMap<>();
    private BundleContext context;
    public static TimerFactoryImpl instance; // TODO use method
    private static LinkedList<SchedulerJob> preSchedule = new LinkedList<>();

    public TimerFactoryImpl() {}

    @Deactivate
    void doDeactivate(ComponentContext ctx) {

        log().i("cancel common timer");
        tracker.stop();
        myTimer.cancel();
        myTimer = null;
        context = null;
        instance = null;
    }

    @Activate
    void doActivate(ComponentContext ctx) {

        instance = this;

        log().i("start common timer");
        myTimer.start();

        // set to base
        try {
            TimerIfc timerIfc = new TimerWrap();
            int cnt = 0;
            while (MApi.get().getLookupActivator().removeObject(TimerIfc.class, null)) {
                cnt++;
                if (cnt > 100) {
                    log().e("can't remove old timer ifc");
                    break;
                }
            }
            MApi.get().getLookupActivator().addObject(TimerIfc.class, null, timerIfc);
        } catch (Throwable t) {
            System.out.println("Can't initialize timer base: " + t);
        }

        context = ctx.getBundleContext();
        tracker =
                new AbstractServiceTracker<SchedulerService>(context, SchedulerService.class) {

                    @Override
                    protected void removeService(
                            ServiceReference<SchedulerService> reference,
                            SchedulerService service) {
                        log().d(
                                        "remove service",
                                        reference.getBundle().getSymbolicName(),
                                        reference.getBundle().getBundleId(),
                                        MOsgi.getState(reference.getBundle()),
                                        service.getClass().getCanonicalName());
                        removeSchedulerService(service);
                    }

                    @Override
                    protected void addService(
                            ServiceReference<SchedulerService> reference,
                            SchedulerService service) {
                        log().d(
                                        "add service",
                                        reference.getBundle().getSymbolicName(),
                                        reference.getBundle().getBundleId(),
                                        MOsgi.getState(reference.getBundle()),
                                        service.getClass().getCanonicalName());
                        addSchedulerService(reference, service);
                    }
                }.start();

        // import preSchedule
        preSchedule.forEach(j -> getTimer().schedule(j));
        preSchedule.clear();
    }

    protected void addSchedulerService(
            ServiceReference<SchedulerService> reference, SchedulerService service) {

        SchedulerJob job = null;
        String interval = service.getInterval();
        String username = service.getUsername();
        job = service.getWrappedJob();

        if (job == null) {
            // get interval configuration
            if (interval == null) interval = MString.valueOf(reference.getProperty("interval"));
            if (interval == null) {
                log().w(
                                "interval configuration not found for SchedulerService",
                                service,
                                reference);
                return;
            }
            // parse configuration and create job
            job = Scheduler.createSchedulerJob(interval, service);
        }
        if (username == null) username = MString.valueOf(reference.getProperty("username"));

        if (job != null) {
            if (job instanceof MutableSchedulerJob)
                ((MutableSchedulerJob)job).setUsername(username);
            job.setNextExecutionTime(SchedulerJob.CALCULATE_NEXT);

            job.setInfo(
                    reference.getBundle().getSymbolicName()
                            + " ["
                            + reference.getBundle().getBundleId()
                            + "]");
            TimerTaskInterceptor intercepter = service.getInterceptor();
            if (intercepter != null) job.setIntercepter(intercepter);
            services.put(service, job);
            myTimer.schedule(job);
        } else {
            log().w(
                            "interval configuration syntax error for SchedulerService",
                            service,
                            reference,
                            interval);
        }
    }

    protected void removeSchedulerService(SchedulerService service) {
        SchedulerJob job = services.get(service);
        if (job != null) {
            job.setNextExecutionTime(SchedulerJob.REMOVE_TIME);
            myTimer.removeJob(job);
        } else {
            log().w("timer task not found for ScheduledService", service);
        }
    }

    public static SchedulerTimer getScheduler(TimerFactory factory) {
        TimerIfc timer = factory.getTimer();
        if (timer instanceof TimerWrap) {
            return ((TimerWrap) timer).getScheduler();
        }
        return null;
    }

    private class TimerWrap implements TimerIfc {

        private void createService(String name, TimerTask task, String interval) {
            Bundle caller = FrameworkUtil.getBundle(task.getClass());
            ScheduledServiceWrap service = new ScheduledServiceWrap(name, caller, task, interval);
            Hashtable<String, Object> properties = new Hashtable<>();
            String n = service.getName();
            properties.put("job.name", n == null ? "?" : n);
            properties.put("job.task", task.getClass());
            properties.put("job.interval", interval);
            properties.put("job.bundle", caller.getSymbolicName());
            properties.put("job.timer", MSystem.getObjectId(this));
            String username = Aaa.getPrincipal();
            if (username != null) {
                properties.put("job.username", username);
                service.setUsername(username);
            }
            if (caller == null || caller.getBundleContext() == null) {
                log().w("Can't register scheduler to others bundle context", service, properties);
                context.registerService(SchedulerService.class, service, properties);
            } else
                caller.getBundleContext()
                        .registerService(SchedulerService.class, service, properties);
        }

        private void createService(SchedulerJob job) {
            Bundle caller =
                    FrameworkUtil.getBundle(
                            job.getTask() == null ? job.getClass() : job.getTask().getClass());
            ScheduledServiceWrap service = new ScheduledServiceWrap(caller, job);
            Hashtable<String, Object> properties = new Hashtable<>();
            String n = job.getName();
            properties.put("job.name", n == null ? "?" : n);
            properties.put("job.scheduler", job.getClass());
            properties.put("job.task", job.getTask().getClass());
            properties.put("job.bundle", caller.getSymbolicName());
            properties.put("job.timer", MSystem.getObjectId(this));
            if (job.getUsername() != null)
                properties.put("job.username", job.getUsername());
            if (caller == null || caller.getBundleContext() == null) {
                log().w("Can't register scheduler to others bundle context", service, properties);
                context.registerService(SchedulerService.class, service, properties);
            } else
                caller.getBundleContext()
                        .registerService(SchedulerService.class, service, properties);
        }

        public SchedulerTimer getScheduler() {
            return myTimer;
        }

        @Override
        public void schedule(TimerTask task, long delay) {
            createService(null, task, "interval:" + delay);
        }

        @Override
        public void schedule(TimerTask task, Date time) {
            createService(null, task, "once:" + MDate.toIso8601(time));
        }

        @Override
        public void schedule(TimerTask task, long delay, long period) {
            createService(null, task, "interval:" + delay + "," + period);
        }

        @Override
        public void schedule(TimerTask task, Date firstTime, long period) {
            createService(null, task, "interval:" + MDate.toIso8601(firstTime) + "," + period);
        }

        @Override
        public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
            schedule(null, task, delay, period);
        }

        @Override
        public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
            schedule(null, task, firstTime, period);
        }

        @Override
        public void schedule(String name, TimerTask task, long delay) {
            createService(name, task, "interval:" + delay);
        }

        @Override
        public void schedule(String name, TimerTask task, Date time) {
            createService(name, task, "once:" + MDate.toIso8601(time));
        }

        @Override
        public void schedule(String name, TimerTask task, long delay, long period) {
            createService(name, task, "interval:" + delay + "," + period);
        }

        @Override
        public void schedule(String name, TimerTask task, Date firstTime, long period) {
            createService(name, task, "interval:" + MDate.toIso8601(firstTime) + "," + period);
        }

        @Override
        public void scheduleAtFixedRate(String name, TimerTask task, long delay, long period) {
            schedule(name, task, delay, period);
        }

        @Override
        public void scheduleAtFixedRate(String name, TimerTask task, Date firstTime, long period) {
            schedule(name, task, firstTime, period);
        }

        @Override
        public void schedule(SchedulerJob job) {
            createService(job);
        }

        @Override
        public void cancel() {
            try {
                for (Service<SchedulerService> ref :
                        MOsgi.getServiceRefs(
                                SchedulerService.class,
                                "(job.timer=" + MSystem.getObjectId(this) + ")"))
                    try {
                        ref.getReference()
                                .getBundle()
                                .getBundleContext()
                                .ungetService(ref.getReference());
                    } catch (Throwable t) {
                        log().d("unset SchedulerService", MSystem.getObjectId(this), ref);
                        log().t(t);
                    }
            } catch (Throwable t) {
                log().d("unable to cancel timers");
                log().t(t);
            }
        }
    }

    public static class ScheduledServiceWrap implements SchedulerService {

        private TimerTask task;
        private String interval;
        private String name;
        private String username;

        public ScheduledServiceWrap(Bundle bundle, SchedulerJob job) {
            task = job;
        }

        public ScheduledServiceWrap(String name, Bundle bundle, TimerTask task, String interval) {
            this.task = task;
            this.interval = interval;
            this.name = name;
        }

        @Override
        public String getInterval() {
            return interval;
        }

        @Override
        public void run(Object environment) throws Exception {
            if (task instanceof ITimerTask) ((ITimerTask) task).run(environment);
            else task.run();
        }

        @Override
        public void onError(Throwable t) {
            if (task instanceof ITimerTask) ((ITimerTask) task).onError(t);
            else t.printStackTrace();
        }

        @Override
        public void onFinal(boolean isError) {
            if (task instanceof ITimerTask) ((ITimerTask) task).onFinal(isError);
            ;
        }

        @Override
        public boolean isCanceled() {
            if (task instanceof ITimerTask) return ((ITimerTask) task).isCanceled();
            return false;
        }

        @Override
        public String getName() {
            if (name != null) return name;
            if (task instanceof Named) return ((Named) task).getName();
            return MSystem.getClassName(task.getClass());
        };

        @Override
        public SchedulerJob getWrappedJob() {
            if (task instanceof SchedulerJob) return (SchedulerJob) task;
            return null;
        }

        @Override
        public TimerTaskInterceptor getInterceptor() {
            if (task instanceof SchedulerJob) return ((SchedulerJob) task).getInterceptor();
            return null;
        }

        @Override
        public String getUsername() {
            if (task instanceof SchedulerJob) return ((SchedulerJob) task).getUsername();
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    @Override
    public TimerIfc getTimer() {
        return new TimerWrap();
    }

    public static void doDebugInfo() {
        for (SchedulerJob job : instance.myTimer.getScheduledJobs()) {
            Object task = job.getTask();
            String info = " ";
            if (task instanceof de.mhus.lib.core.schedule.TimerTaskAdapter) {
                task = ((de.mhus.lib.core.schedule.TimerTaskAdapter) task).getTask();
                info += "ObserverTimerTaskAdapter ";
            }
            log.i(
                    "JOB",
                    job.getClass(),
                    job.getName(),
                    info,
                    task == null ? "null" : task.getClass());
        }
    }

    public void stop() {
        tracker.stop();
        MThread.sleep(1000);
        myTimer.clear();
        myTimer.stop();
    }

    public void start() {
        myTimer.start();
        tracker.start();
    }

    public boolean isRunning() {
        return tracker.isRunning();
    }

    public static void schedule(SchedulerJob job) {
        if (instance != null) instance.getTimer().schedule(job);
        else preSchedule.add(job);
    }
}
