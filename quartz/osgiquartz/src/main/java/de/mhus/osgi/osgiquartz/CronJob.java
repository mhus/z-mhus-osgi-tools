package de.mhus.osgi.osgiquartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public abstract class CronJob extends AbstractQuargiJob {

	protected Trigger createTrigger() {
		return TriggerBuilder.newTrigger()
	    .withIdentity(getJobName(), getJobGroup())
	    .withSchedule(CronScheduleBuilder.cronSchedule(getCronExpression()))
	    .build();
	}

	protected abstract String getCronExpression();

}
