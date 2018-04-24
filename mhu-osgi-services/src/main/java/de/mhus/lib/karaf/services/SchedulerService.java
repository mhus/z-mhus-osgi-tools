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
package de.mhus.lib.karaf.services;

import de.mhus.lib.core.ITimerTask;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.TimerTaskIntercepter;

/**
 * Create this interface as a component and the timer will schedule the observer as a timer task.
 * Use the parameter 'interval' to define the default interval as time or cron job definition.
 * interval=15m - every 15 minutes from start (do not insert spaces!)
 * interval=1m,1h - first execution after 1 minute then every hour (do not insert spaces!)
 * interval=1,15,30,45 * * * * * Every 15 minutes exact every quarter hour
 * 
 * @author mikehummel
 *
 */
public interface SchedulerService extends ITimerTask {

	/**
	 * Overwrite interval defined in the component parameters. Return null if you don't need to
	 * define a customized interval.
	 * 
	 * @return Interval
	 */
	String getInterval();

	SchedulerJob getWrappedJob();

	/**
	 * Return a intercepter to handle this
	 * @return intercepter
	 */
	TimerTaskIntercepter getIntercepter();
	
}
