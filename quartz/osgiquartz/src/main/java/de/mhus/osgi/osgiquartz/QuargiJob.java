package de.mhus.osgi.osgiquartz;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public interface QuargiJob {

	JobDetail getJob() throws ClassNotFoundException;

	Trigger getTrigger() throws ClassNotFoundException;

	void errorEvent(Exception e);

}
