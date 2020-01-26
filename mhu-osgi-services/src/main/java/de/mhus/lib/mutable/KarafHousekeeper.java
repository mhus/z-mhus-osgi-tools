/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.lib.mutable;

import java.lang.ref.WeakReference;
import java.util.Map.Entry;

import de.mhus.lib.core.ITimerTask;
import de.mhus.lib.core.MHousekeeper;
import de.mhus.lib.core.MHousekeeperTask;
import de.mhus.lib.core.lang.MObject;
import de.mhus.lib.core.schedule.IntervalJob;
import de.mhus.lib.core.system.DefaultHousekeeper;
import de.mhus.osgi.services.scheduler.TimerFactoryImpl;

public class KarafHousekeeper extends MObject implements MHousekeeper {

    public KarafHousekeeper() {
        for (Entry<MHousekeeperTask, Long> task : DefaultHousekeeper.getAll().entrySet()) {
            String name = task.getKey().getName();
            log().d("import", name, task.getKey(), task.getValue());

            WeakObserver t = new WeakObserver(task.getKey(), "housekeeper:" + name);
            IntervalJob job = new IntervalJob(task.getValue(), t);
            t.setJob(job);
            TimerFactoryImpl.schedule(job);
        }
    }

    @Override
    public void register(MHousekeeperTask task, long sleep) {
        String name = task.getName();
        log().d("register", name, task, sleep);
        DefaultHousekeeper.put(task, sleep);

        WeakObserver t = new WeakObserver(task, "housekeeper:" + name);
        IntervalJob job = new IntervalJob(sleep, t);
        t.setJob(job);
        TimerFactoryImpl.schedule(job);
    }

    private static class WeakObserver implements ITimerTask {

        private WeakReference<MHousekeeperTask> task;
        private IntervalJob job;
        private String name;

        public WeakObserver(MHousekeeperTask task, String name) {
            this.task = new WeakReference<MHousekeeperTask>(task);
            this.name = name;
        }

        public void setJob(IntervalJob job) {
            this.job = job;
        }

        @Override
        public void run(Object arg) {
            MHousekeeperTask t = task.get();
            if (t == null) job.cancel();
            else t.run(arg);
        }

        @Override
        public String toString() {
            MHousekeeperTask t = task.get();
            if (t == null) return name + "[removed]";
            return t.toString();
        }

        @Override
        public String getName() {
            MHousekeeperTask t = task.get();
            if (t == null) return name + "[removed]";
            return name;
        }

        @Override
        public void onError(Throwable e) {
            MHousekeeperTask t = task.get();
            if (t == null) job.cancel();
            else t.onError(e);
        }

        @Override
        public void onFinal(boolean isError) {
            MHousekeeperTask t = task.get();
            if (t == null) job.cancel();
            else t.onFinal(isError);
        }

        @Override
        public boolean isCanceled() {
            MHousekeeperTask t = task.get();
            if (t == null) return true;
            return t.isCanceled();
        }
    }
}
