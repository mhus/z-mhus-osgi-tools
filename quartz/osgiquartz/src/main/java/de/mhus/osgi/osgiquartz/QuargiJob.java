package de.mhus.osgi.osgiquartz;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public interface QuargiJob {

	JobDetail getJob();

	Trigger getTrigger();

	void errorEvent(Exception e);

}
