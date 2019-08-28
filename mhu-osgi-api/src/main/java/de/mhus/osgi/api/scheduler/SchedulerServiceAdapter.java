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
package de.mhus.osgi.api.scheduler;

import de.mhus.lib.annotations.util.Interval;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.TimerTaskInterceptor;

public abstract class SchedulerServiceAdapter extends MLog implements SchedulerService {

	protected boolean canceled = false;

	@Override
	public void onError(Throwable t) {
		
	}

	@Override
	public void onFinal(boolean isError) {
		
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public String getName() {
		return getClass().getCanonicalName();
	}

	@Override
	public String getInterval() {
		Interval interval = getClass().getAnnotation(Interval.class);
		if (interval != null) {
			if (MString.isSet(interval.cfgPath())) {
				String path = interval.cfgPath();
				Class<?> owner = interval.cfgOwner();
				if (owner == Class.class)
					owner = getClass();
				return MApi.get().getCfgString(owner, path, interval.value());
			} else
				return interval.value();
		}
		return null;
	}

	@Override
	public SchedulerJob getWrappedJob() {
		return null;
	}

	@Override
	public TimerTaskInterceptor getInterceptor() {
		return null;
	}

}
