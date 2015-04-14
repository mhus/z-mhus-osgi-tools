package de.mhus.osgi.osgiquartz;

import org.osgi.framework.FrameworkUtil;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class CronJob extends AbstractQuargiJob {

	private String cronExpression;
	
	protected Trigger createTrigger() throws ClassNotFoundException {
		return TriggerBuilder.newTrigger()
	    .withIdentity(getJobName(), getJobGroup())
	    .withSchedule(CronScheduleBuilder.cronSchedule(getCronExpression()))
	    .build();
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

}
