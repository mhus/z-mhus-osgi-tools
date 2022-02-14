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
package de.mhus.karaf.commands.mhus;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimerTask;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.schedule.MutableSchedulerJob;
import de.mhus.lib.core.schedule.OnceJob;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.SchedulerTimer;
import de.mhus.lib.core.service.TimerFactory;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.services.scheduler.TimerFactoryImpl;

@Command(scope = "mhus", name = "timer-tools", description = "Default timer tools")
@Service
public class CmdTimerTools extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command to execute\n"
                            + " timeout\n"
                            + " stacktrace\n"
                            + " timeoutstacktrace\n"
                            + " dummy\n"
                            + " schedule\n"
                            + " configure\n"
                            + " debug\n"
                            + " recreate\n"
                            + " start\n"
                            + " stop\n"
                            + " trail",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {
        TimerFactory factory = MOsgi.getService(TimerFactory.class);
        SchedulerTimer scheduler = TimerFactoryImpl.getScheduler(factory);

        if (!TimerFactoryImpl.instance.isRunning()) {
            System.out.println("ServiceTracker is not running!");
        }

        if (cmd.equals("username")) {
            for (SchedulerJob job : getScheduledJob(scheduler, parameters[0])) {
                if (job != null) {
                    if (job instanceof MutableSchedulerJob)
                        ((MutableSchedulerJob) job).setUsername(parameters[1]);
                }
            }
        }
        if (cmd.equals("timeout")) {
            List<SchedulerJob> running = scheduler.getRunningJobs();

            ConsoleTable table = new ConsoleTable(tblOpt);
            table.setHeaderValues(
                    "Task",
                    "Job",
                    "Started",
                    "Stopped",
                    "Description",
                    "Name",
                    "Scheduled",
                    "Timeout");

            long time = System.currentTimeMillis();
            for (SchedulerJob job : running) {
                long timeout = job.getTimeoutInMinutes() * MPeriod.MINUTE_IN_MILLISECONDS;
                if (timeout > 0 && timeout + job.getLastExecutionStart() <= time) {
                    table.addRowValues(
                            job.getTask(),
                            job,
                            MDate.toIsoDateTime(job.getLastExecutionStart()),
                            "Running",
                            job.getDescription(),
                            job.getName(),
                            "Running",
                            job.getTimeoutInMinutes());
                }
            }

            table.print(System.out);
        }
        if (cmd.equals("stacktrace")) {
            List<SchedulerJob> running = scheduler.getRunningJobs();
            for (SchedulerJob job : running) {
                Thread thread = job.getThread();
                if (thread != null) {
                    StackTraceElement[] stack = thread.getStackTrace();
                    System.out.println(
                            MCast.toString(
                                    job.getName() + " (threadId=" + thread.getId() + ")", stack));
                }
            }
        }
        if (cmd.equals("timeoutstacktrace")) {
            List<SchedulerJob> running = scheduler.getRunningJobs();
            long time = System.currentTimeMillis();
            for (SchedulerJob job : running) {
                long timeout = job.getTimeoutInMinutes() * MPeriod.MINUTE_IN_MILLISECONDS;
                if (timeout > 0 && timeout + job.getLastExecutionStart() <= time) {
                    Thread thread = job.getThread();
                    if (thread != null) {
                        StackTraceElement[] stack = thread.getStackTrace();
                        System.out.println(
                                MCast.toString(
                                        job.getName() + " (threadId=" + thread.getId() + ")",
                                        stack));
                    }
                }
            }
        }
        if (cmd.equals("dummy")) {
            scheduler.schedule(
                    new OnceJob(
                            System.currentTimeMillis() + MPeriod.MINUTE_IN_MILLISECONDS,
                            new MTimerTask() {

                                @Override
                                protected void doit() throws Exception {
                                    log().i(">>> Start Dummy");
                                    MThread.sleep(MPeriod.MINUTE_IN_MILLISECONDS * 2);
                                    log().i("<<< Stop Dummy");
                                }
                            }) {
                        {
                            setTimeoutInMinutes(1);
                        }

                        @Override
                        public void doTimeoutReached() {
                            log().i("+++ Dummy Timeout Reached");
                        }
                    });
        }
        if (cmd.equals("schedule")) {
            Date time = MCast.toDate(parameters[1], null);
            if (time == null) {
                System.out.println("Malformed time");
                return null;
            }
            for (SchedulerJob job : getScheduledJob(scheduler, parameters[0])) {
                if (job != null && job instanceof MutableSchedulerJob) {
                    ((MutableSchedulerJob) job).doReschedule(scheduler, time.getTime());
                    System.out.println(
                            "OK, Scheduled " + job.getName() + " to " + MDate.toIsoDateTime(time));
                }
            }
        }

        if (cmd.equals("configure")) {
            for (SchedulerJob job : getScheduledJob(scheduler, parameters[0])) {
                if (job != null) {
                    if (job instanceof MutableSchedulerJob) {
                        boolean ret = ((MutableSchedulerJob) job).doReconfigure(parameters[1]);
                        System.out.println("OK " + job.getName() + " " + ret);
                        if (ret)
                            ((MutableSchedulerJob) job)
                                    .doReschedule(scheduler, SchedulerJob.CALCULATE_NEXT);
                    }
                }
            }
        }

        if (cmd.equals("debug")) {
            TimerFactoryImpl.doDebugInfo();
        }
        if (cmd.equals("recreate")) {
            TimerFactoryImpl.instance.stop();
            TimerFactoryImpl.instance.start();
            System.out.println("OK");
        }
        if (cmd.equals("start")) {
            TimerFactoryImpl.instance.start();
            System.out.println("OK");
        }
        if (cmd.equals("stop")) {
            TimerFactoryImpl.instance.stop();
            System.out.println("OK");
        }
        if (cmd.equals("trail")) {
            for (SchedulerJob job : getScheduledJob(scheduler, parameters[0])) {
                if (job != null) {
                    System.out.println("Change " + job.getName());
                }
            }
        }

        return null;
    }

    private List<SchedulerJob> getScheduledJob(SchedulerTimer scheduler, String jobId) {
        List<SchedulerJob> jobs = scheduler.getScheduledJobs();
        LinkedList<SchedulerJob> out = new LinkedList<>();
        for (SchedulerJob job : jobs) {
            if (MString.compareFsLikePattern(job.getName(), jobId)) out.add(job);
        }
        return out;
    }
}
