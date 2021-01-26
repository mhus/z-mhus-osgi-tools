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

import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.schedule.MutableSchedulerJob;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.SchedulerTimer;
import de.mhus.lib.core.service.TimerFactory;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.services.scheduler.TimerFactoryImpl;

@Command(scope = "mhus", name = "timer-disable", description = "Disable a timer job")
@Service
public class CmdTimerDisable extends AbstractCmd {

    @Argument(
            index = 0,
            name = "job",
            required = true,
            description = "Name of the job",
            multiValued = false)
    String jobName;

    @Override
    public Object execute2() throws Exception {
        TimerFactory factory = MOsgi.getService(TimerFactory.class);
        SchedulerTimer scheduler = TimerFactoryImpl.getScheduler(factory);

        if (!TimerFactoryImpl.instance.isRunning()) {
            System.out.println("ServiceTracker is not running!");
        }


        for (SchedulerJob job : getScheduledJob(scheduler, jobName)) {
            if (job != null && job instanceof MutableSchedulerJob) {
                ((MutableSchedulerJob) job).doReschedule(scheduler, SchedulerJob.DISABLED_TIME);
                System.out.println("OK " + job.getName());
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
