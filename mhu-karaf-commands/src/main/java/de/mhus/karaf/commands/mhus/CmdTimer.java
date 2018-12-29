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
package de.mhus.karaf.commands.mhus;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.MTimerTask;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.schedule.MutableSchedulerJob;
import de.mhus.lib.core.schedule.OnceJob;
import de.mhus.lib.core.schedule.SchedulerJob;
import de.mhus.lib.core.schedule.SchedulerTimer;
import de.mhus.osgi.services.MOsgi;
import de.mhus.osgi.services.scheduler.TimerFactoryImpl;

@Command(scope = "mhus", name = "timer", description = "Default Timer Handling")
@Service
public class CmdTimer extends MLog implements Action {

	@Argument(index=0, name="cmd", required=true, description=
			"Command to execute"
			+ " list,\n"
			+ " timeout,\n"
			+ " stacktrace,\n"
			+ " timeoutstacktrace,\n"
//			+ " schedule <name> <time>,\n"
			+ " done <name> <done>,\n"
			+ " disable/enable/cancel/remove <name>,\n"
			+ " run <name>,\n"
			+ " configure <name> <configuration>\n"
			+ " trail <name> [<trail config>]\n"
			+ " release <name> - hard release the busy lock"
			+ "  name is allways a pattern use * to define placeholders", multiValued=false)
    String cmd;

	@Argument(index=1, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;
	
	@Option(name="-a", aliases="--all", description="return all informations",required=false)
	boolean all = false;

	@Option(name="-f", aliases="--full", description="Full output",required=false)
	boolean full = false;

	@Override
	public Object execute() throws Exception {
		TimerFactory factory = MOsgi.getService(TimerFactory.class);
		SchedulerTimer scheduler = TimerFactoryImpl.getScheduler(factory);
		
		if (!TimerFactoryImpl.instance.isRunning()) {
			System.out.println("ServiceTracker is not running!");
		}
		
		if (cmd.equals("jobs")) {
			List<SchedulerJob> scheduled = scheduler.getJobs();
			ConsoleTable table = new ConsoleTable(full);
			if (all)
				table.setLineSpacer(true);
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
						"Trail config",
						"Busy"
					);
			else
				table.setHeaderValues(
						"Name",
						"Job",
						"Info",
						"Started",
						"Scheduled/Thread",
						"Left",
						"Canceled",
						"Status"
					);
		
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
							MTimeInterval.getIntervalAsStringSec(job.getScheduledTime() - System.currentTimeMillis()),
							job.getTimeoutInMinutes(),
							job.isCanceled(),
							job.isDone(),
							getStatus(job),
							job.getIntercepter(),
							job.getLogTrailConfig(),
							job.isBusy()
						);
				else
					table.addRowValues(
							job.getName(),
							job,
							job.getInfo(),
							MDate.toIsoDateTime(job.getLastExecutionStart()),
							MDate.toIsoDateTime(job.getScheduledTime()), 
							MTimeInterval.getIntervalAsStringSec(job.getScheduledTime() - System.currentTimeMillis()),
							job.isCanceled(),
							getStatus(job)
						);

			}
			
			table.print(System.out);
			
		} else
		if (cmd.equals("list")) {
			
			
			List<SchedulerJob> scheduled = scheduler.getScheduledJobs();
			List<SchedulerJob> running = scheduler.getRunningJobs();
			
			ConsoleTable table = new ConsoleTable(full);
			if (all)
				table.setLineSpacer(true);
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
						"Trail config",
						"Busy"
					);
			else
				table.setHeaderValues(
						"Name",
						"Job",
						"Info",
						"Started",
						"Scheduled/Thread",
						"Left",
						"Canceled",
						"Status"
					);
		
			for (SchedulerJob job : running) {
				if (all)
					table.addRowValues(
							job.getName(),
							job.getTask(),
							job,
							job.getInfo(),
							MDate.toIsoDateTime(job.getLastExecutionStart()),
							"Running",
							job.getThread().getId() + " " + job.getThread(),
							"-",
							job.getTimeoutInMinutes(),
							job.isCanceled(),
							job.isDone(),
							getStatus(job),
							job.getIntercepter(),
							job.getLogTrailConfig(),
							job.isBusy()
						);
				else
					table.addRowValues(
							job.getName(),
							job,
							job.getInfo(),
							MDate.toIsoDateTime(job.getLastExecutionStart()),
							job.getThread().getId() + " " + job.getThread(),
							"-",
							job.isCanceled(),
							getStatus(job)
						);
			}
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
							MTimeInterval.getIntervalAsStringSec(job.getScheduledTime() - System.currentTimeMillis()),
							job.getTimeoutInMinutes(),
							job.isCanceled(),
							job.isDone(),
							getStatus(job),
							job.getIntercepter(),
							job.getLogTrailConfig(),
							job.isBusy()
						);
				else
					table.addRowValues(
							job.getName(),
							job,
							job.getInfo(),
							MDate.toIsoDateTime(job.getLastExecutionStart()),
							MDate.toIsoDateTime(job.getScheduledTime()), 
							MTimeInterval.getIntervalAsStringSec(job.getScheduledTime() - System.currentTimeMillis()),
							job.isCanceled(),
							getStatus(job)
						);

			}
			
			table.print(System.out);
		}
		if (cmd.equals("timeout")) {
			List<SchedulerJob> running = scheduler.getRunningJobs();
			
			ConsoleTable table = new ConsoleTable(full);
			table.setHeaderValues("Task","Job","Started","Stopped", "Description","Name","Scheduled","Timeout");
			
			long time = System.currentTimeMillis();
			for (SchedulerJob job : running) {
				long timeout = job.getTimeoutInMinutes() * MTimeInterval.MINUTE_IN_MILLISECOUNDS;
				if (timeout > 0 && timeout + job.getLastExecutionStart() <= time) {
					table.addRowValues(job.getTask(),job,MDate.toIsoDateTime(job.getLastExecutionStart()),"Running",job.getDescription(),job.getName(),"Running",job.getTimeoutInMinutes());
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
					System.out.println( MCast.toString(job.getName() + " (threadId=" + thread.getId() + ")",stack) );
				}
			}
		}
		if (cmd.equals("timeoutstacktrace")) {
			List<SchedulerJob> running = scheduler.getRunningJobs();
			long time = System.currentTimeMillis();
			for (SchedulerJob job : running) {
				long timeout = job.getTimeoutInMinutes() * MTimeInterval.MINUTE_IN_MILLISECOUNDS;
				if (timeout > 0 && timeout + job.getLastExecutionStart() <= time) {
					Thread thread = job.getThread();
					if (thread != null) {
						StackTraceElement[] stack = thread.getStackTrace();
						System.out.println( MCast.toString(job.getName() + " (threadId=" + thread.getId() + ")",stack) );
					}
				}
			}
		}
		if (cmd.equals("dummy")) {
			scheduler.schedule(new OnceJob(System.currentTimeMillis() + MTimeInterval.MINUTE_IN_MILLISECOUNDS, new MTimerTask() {
				
				@Override
				protected void doit() throws Exception {
					log().i(">>> Start Dummy");
					MThread.sleep(MTimeInterval.MINUTE_IN_MILLISECOUNDS * 2);
					log().i("<<< Stop Dummy");
				}
			})
			{
				{
					setTimeoutInMinutes(1);
				}
				
				@Override
				public void doTimeoutReached() {
					log().i("+++ Dummy Timeout Reached");
				}
			}
			);
		}
		if (cmd.equals("schedule")) {
			Date time = MCast.toDate(parameters[1], null);
			if (time == null) {
				System.out.println("Malformed time");
				return null;
			}
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null && job instanceof MutableSchedulerJob) {
					((MutableSchedulerJob)job).setScheduledTime(time.getTime());

					System.out.println("OK, Scheduled " + job.getName() +" to " + MDate.toIsoDateTime( time ) );
				}
			}
		}
		if (cmd.equals("done")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null && job instanceof MutableSchedulerJob) {
					((MutableSchedulerJob)job).setDone( MCast.toboolean(parameters[1], false));
					
					System.out.println("OK " + job.getName());
				}
			}
		}
		if (cmd.equals("remove")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null) {
					scheduler.getQueue().removeJob(job);
					System.out.println("OK " + job.getName());
				}
			}
		}
		if (cmd.equals("disable")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null && job instanceof MutableSchedulerJob) {
					((MutableSchedulerJob)job).doReschedule(scheduler, SchedulerJob.DISABLED_TIME);
					System.out.println("OK " + job.getName());
				}
			}
		}
		if (cmd.equals("enable")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null && job instanceof MutableSchedulerJob) {
					((MutableSchedulerJob)job).doReschedule(scheduler, SchedulerJob.CALCULATE_NEXT);
					System.out.println("OK " + job.getName());
				}
			}
		}
		if (cmd.equals("cancel")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null) {
					job.cancel();
					System.out.println("OK " + job.getName());
				}
			}
		}
		if (cmd.equals("release")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null) {
					job.releaseBusy(null);
					System.out.println("OK " + job.getName());
				}
			}
		}
		if (cmd.equals("configure")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null) {
					if (job instanceof MutableSchedulerJob) {
						boolean ret = ((MutableSchedulerJob)job).doReconfigure(parameters[1]);
						System.out.println("OK " + job.getName() + " " + ret);
						if (ret)
							((MutableSchedulerJob) job).doReschedule(scheduler, SchedulerJob.CALCULATE_NEXT);
					}
				}
			}
		}
		if (cmd.equals("run")) {
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null) {
					System.out.println("RUN " + job.getName());
					scheduler.doExecuteJob(job, true);
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
			for (SchedulerJob job : getScheduledJob(scheduler, parameters[0]) ) {
				if (job != null) {
					System.out.println("Change " + job.getName());
					if (parameters.length < 2 || MString.isEmpty(parameters[1]))
						job.setLogTrailConfig(null);
					else
						job.setLogTrailConfig(parameters[1]);
				}
			}
		}
		
		return null;
	}

	private String getStatus(SchedulerJob job) {
		long t = job.getNextExecutionTime();
		if (t == SchedulerJob.CALCULATE_NEXT) return "Calculate";
		if (t == SchedulerJob.DISABLED_TIME) return "Disabled";
		if (t == SchedulerJob.REMOVE_TIME) return "Remove";
		return "OK";
	}

	private List<SchedulerJob> getScheduledJob(SchedulerTimer scheduler, String jobId) {
		List<SchedulerJob> jobs = scheduler.getScheduledJobs();
		LinkedList<SchedulerJob> out = new LinkedList<>();
		for (SchedulerJob job : jobs) {
			if (MString.compareFsLikePattern(job.getName(),jobId)) out.add(job);
		}
		return out;
	}

}
