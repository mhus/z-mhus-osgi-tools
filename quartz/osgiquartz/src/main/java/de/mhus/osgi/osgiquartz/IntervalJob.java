package de.mhus.osgi.osgiquartz;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public abstract class IntervalJob extends AbstractQuargiJob {

	protected Trigger createTrigger() {
		return TriggerBuilder.newTrigger()
	    .withIdentity(getJobName(), getJobGroup())
	    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(getIntervalInSeconds()).repeatForever())
	    .build();
	}

	protected abstract int getIntervalInSeconds();
	
}
