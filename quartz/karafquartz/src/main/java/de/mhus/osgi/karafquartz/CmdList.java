package de.mhus.osgi.karafquartz;

import java.util.Date;
import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.osgi.framework.FrameworkUtil;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import aQute.bnd.annotation.component.Attribute;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.osgiquartz.Quargi;
import de.mhus.osgi.osgiquartz.QuargiJob;

@Command(scope = "quargi", name = "list", description = "List current quarz jobs")
public class CmdList implements Action {

	private Quargi quargi;

	@Option(name="-c",aliases="--current",description="Show currently executing jobs",multiValued=false,required=false)
	private boolean current = false;
	
	@Override
	public Object execute(CommandSession session) throws Exception {

		
		if (!current) {
			
			Scheduler scheduler = quargi.getScheduler();

			if (scheduler.isShutdown())
				System.out.println( "Status: shutdown" );
			else
			if (scheduler.isInStandbyMode())
				System.out.println( "Status: standby" );
			else
			if (scheduler.isStarted())
				System.out.println( "Status: started" );
				
			ConsoleTable table = new ConsoleTable();
			table.setHeaderValues("Group","Name","Class","Bundle","Trigger");
			
			for (QuargiJob job : quargi.getJobs()) {
				JobDetail details = job.getJob();
				
				String group = details.getKey().getGroup();
				String name = details.getKey().getName();
				String clazz = details.getJobClass().getName();
				String bundleName = FrameworkUtil.getBundle(details.getJobClass()).getSymbolicName();
				
				String trigger = "?";
				if (scheduler.checkExists(details.getKey())) {
					List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(details.getKey());
					if (triggerList != null && triggerList.size() > 0)
						trigger = triggerList.toString();
				}
				table.addRowValues(group,name,clazz,bundleName,trigger);

			}
			table.print(System.out);
		} else {
			Scheduler scheduler = quargi.getScheduler();
			ConsoleTable table = new ConsoleTable();
			table.setHeaderValues("Group","Name","Class","Bundle","Runtime","LastFireTime","NextFireTime","Trigger");
	
			for (JobExecutionContext ctx : scheduler.getCurrentlyExecutingJobs()) {
				JobDetail details = ctx.getJobDetail();
				
				String group = details.getKey().getGroup();
				String name = details.getKey().getName();
				String clazz = details.getJobClass().getName();
				String bundleName = FrameworkUtil.getBundle(details.getJobClass()).getSymbolicName();
				long runtime = ctx.getJobRunTime();
				Date fireTime = ctx.getFireTime();
				Date nextTime = ctx.getNextFireTime();
				String trigger = ctx.getTrigger().toString();
				table.addRowValues(group,name,clazz,bundleName,""+runtime,MDate.toIsoDateTime(fireTime),MDate.toIsoDateTime(nextTime),trigger);
			}
			table.print(System.out);
		}
		return null;
	}

	
	public void setQuargi(Quargi quargi) {
		this.quargi = quargi;
	}
}
