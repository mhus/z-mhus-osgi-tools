package de.mhus.osgi.osgiquartz;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class IntervalJob extends AbstractQuargiJob {

	private int intervalInSeconds;

	protected Trigger createTrigger() throws ClassNotFoundException {
		return TriggerBuilder.newTrigger()
	    .withIdentity(getJobName(), getJobGroup())
	    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(getIntervalInSeconds()).repeatForever())
	    .build();
	}

	protected int getIntervalInSeconds() {
		return intervalInSeconds;
	}

	public void setIntervalInSeconds(int intervalInSeconds) {
		this.intervalInSeconds = intervalInSeconds;
	}

}
