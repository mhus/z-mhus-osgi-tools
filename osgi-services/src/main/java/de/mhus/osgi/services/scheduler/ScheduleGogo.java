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

import java.io.ByteArrayInputStream;

import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MTimerTask;
import de.mhus.lib.core.schedule.Scheduler;
import de.mhus.lib.core.service.TimerFactory;
import de.mhus.lib.core.service.TimerIfc;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.services.ISimpleService;

public class ScheduleGogo extends MLog implements ISimpleService {

    private String interval;
    private String command;
    private TimerIfc timer;
    private MTimerTask job;
    private String name;

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
        log().d(name, "get interval", interval);
        doInit();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setTimerFactory(TimerFactory factory) {
        timer = factory.getTimer();
        log().d(name, "get timer factory");
        doInit();
    }

    private void doInit() {
        if (timer == null || interval == null) return;
        if (job != null) return;
        log().d(name, "start", interval);
        job = new MyJob();
        timer.schedule(Scheduler.createSchedulerJob(interval, job));
    }

    protected void doExecute() {
        if (command == null || timer == null) return;
        log().d(name, "execute", command);

        //        StreamToLogAdapter out = new StreamToLogAdapter(LEVEL.INFO, null);
        //        StreamToLogAdapter err = new StreamToLogAdapter(LEVEL.ERROR, null);
        try {
            SessionFactory commandProcessor = MOsgi.getService(SessionFactory.class);

            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);

            Session commandSession = commandProcessor.create(in, System.out, System.err);

            commandSession.put("interactive.mode", false);
            commandSession.put("APPLICATION", System.getProperty("karaf.name", "root"));
            commandSession.put("USER", "karaf");

            commandSession.execute(command);
        } catch (Throwable t) {
            log().w(name, t);
        }

        //        out.close();
        //        err.close();
    }

    public void init() {}

    @Override
    protected void finalize() throws Throwable {
        destroy();
    };

    public void destroy() {
        if (timer != null) {
            log().d(name, "deactivate");
            timer.cancel();
        }
        timer = null;
        job = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSimpleServiceInfo() {
        return interval;
    }

    @Override
    public String getSimpleServiceStatus() {
        if (timer == null) return "no timer";
        if (job == null) return "not started";
        return "running";
    }

    @Override
    public void doSimpleServiceCommand(String cmd, Object... param) {
        //		if (cmd.equals("restart"))
    }

    private class MyJob extends MTimerTask {

        @Override
        public String getName() {
            return "SchedulerGogo:" + name;
        }

        @Override
        public void doit() {
            doExecute();
        }
    }
}
