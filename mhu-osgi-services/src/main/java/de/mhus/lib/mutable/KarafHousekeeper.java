/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.lib.mutable;

import java.lang.ref.WeakReference;

import de.mhus.lib.core.ITimerTask;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MHousekeeper;
import de.mhus.lib.core.MHousekeeperTask;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.lang.MObject;
import de.mhus.lib.core.schedule.IntervalJob;

public class KarafHousekeeper extends MObject implements MHousekeeper {

	@Override
	public void register(MHousekeeperTask task, long sleep, boolean weak) {
		log().d("register",task,sleep,weak);
		TimerIfc timer = MApi.lookup(TimerIfc.class);
		if (weak) {
			WeakObserver t = new WeakObserver(task);
			IntervalJob job = new IntervalJob(sleep, t);
			t.setJob(job);
			timer.schedule(job);
		} else
			timer.schedule(new IntervalJob(sleep, task));
	}

	private static class WeakObserver implements ITimerTask {

		private WeakReference<MHousekeeperTask> task;
		private IntervalJob job;

		public WeakObserver(MHousekeeperTask task) {
			this.task = new WeakReference<MHousekeeperTask>(task);
		}

		public void setJob(IntervalJob job) {
			this.job = job;
		}

		@Override
		public void run(Object arg) {
			MHousekeeperTask t = task.get();
			if (t == null)
				job.cancel();
			else
				t.run(arg);
		}
		
		@Override
		public String toString() {
			MHousekeeperTask t = task.get();
			if (t == null)
				return super.toString();
			return t.toString();
		}

		@Override
		public String getName() {
			MHousekeeperTask t = task.get();
			if (t == null)
				return "[removed]";
			return t.getName();
		}

		@Override
		public void onError(Throwable e) {
			MHousekeeperTask t = task.get();
			if (t == null)
				job.cancel();
			else
				t.onError(e);
		}

		@Override
		public void onFinal(boolean isError) {
			MHousekeeperTask t = task.get();
			if (t == null)
				job.cancel();
			else
				t.onFinal(isError);
		}

		@Override
		public boolean isCanceled() {
			MHousekeeperTask t = task.get();
			if (t == null)
				return true;
			return t.isCanceled();
		}
		
	}
}
