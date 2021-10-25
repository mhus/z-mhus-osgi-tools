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

import java.util.List;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.SchedulerTimer;
import de.mhus.lib.core.service.TimerFactory;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.services.scheduler.TimerFactoryImpl;

@Command(scope = "mhus", name = "timer-jobs", description = "Default Timer Handling")
@Service
public class CmdTimerJobs extends AbstractCmd {

    @Option(
            name = "-a",
            aliases = "--all",
            description = "return all informations",
            required = false)
    boolean all = false;

    @Override
    public Object execute2() throws Exception {
        TimerFactory factory = MOsgi.getService(TimerFactory.class);
        SchedulerTimer scheduler = TimerFactoryImpl.getScheduler(factory);

        if (!TimerFactoryImpl.instance.isRunning()) {
            System.out.println("ServiceTracker is not running!");
        }

        List<SchedulerJob> scheduled = scheduler.getJobs();
        ConsoleTable table = new ConsoleTable(tblOpt);
        if (all) table.setLineSpacer(true);
        if (all)
            table.setHeaderValues(
                    "Name",
                    "Task",
                    "Job",
                    "Info",
                    "Started",
                    "Stopped",
                    "Scheduled/Thread",
                    "Left",
                    "Timeout",
                    "Canceled",
                    "Done",
                    "Status",
                    "Interceptor",
                    "Trail caller",
                    "Busy");
        else
            table.setHeaderValues(
                    "Name",
                    "Job",
                    "Info",
                    "Started",
                    "Scheduled/Thread",
                    "Left",
                    "Canceled",
                    "Status");

        for (SchedulerJob job : scheduled) {
            if (all)
                table.addRowValues(
                        job.getName(),
                        job.getTask(),
                        job,
                        job.getInfo(),
                        MDate.toIsoDateTime(job.getLastExecutionStart()),
                        MDate.toIsoDateTime(job.getLastExecutionStop()),
                        MDate.toIsoDateTime(job.getScheduledTime()),
                        MPeriod.getIntervalAsStringSec(
                                job.getScheduledTime() - System.currentTimeMillis()),
                        job.getTimeoutInMinutes(),
                        job.isCanceled(),
                        job.isDone(),
                        getStatus(job),
                        job.getInterceptor(),
                        job.getLogTrailCaller(),
                        job.isBusy());
            else
                table.addRowValues(
                        job.getName(),
                        job,
                        job.getInfo(),
                        MDate.toIsoDateTime(job.getLastExecutionStart()),
                        MDate.toIsoDateTime(job.getScheduledTime()),
                        MPeriod.getIntervalAsStringSec(
                                job.getScheduledTime() - System.currentTimeMillis()),
                        job.isCanceled(),
                        getStatus(job));
        }

        table.print(System.out);

        return null;
    }

    private String getStatus(SchedulerJob job) {
        long t = job.getNextExecutionTime();
        if (t == SchedulerJob.CALCULATE_NEXT) return "Calculate";
        if (t == SchedulerJob.DISABLED_TIME) return "Disabled";
        if (t == SchedulerJob.REMOVE_TIME) return "Remove";
        return "OK";
    }
}
