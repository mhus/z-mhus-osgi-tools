package de.mhus.osgi.osgiquartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;

public abstract class AbstractQuargiJob implements QuargiJob {

	private JobDetail job;
	private Trigger trigger;
	
	@Override
	public JobDetail getJob() {
		if (job == null)
			job = createJob();
		return job;
	}

	@Override
	public Trigger getTrigger() {
		if (trigger == null)
			trigger = createTrigger();
		return trigger;
	}

	protected abstract Trigger createTrigger();
	
	protected JobDetail createJob() {
		return JobBuilder.newJob(getJobClass()).withIdentity(getJobName(),getJobGroup()).withDescription(getJobDescription()).build();
	}
	
	protected String getJobGroup() {
		return getJobClass().getPackage().getName();
	}

	protected String getJobDescription() {
		return getClass().getCanonicalName();
	}

	protected String getJobName() {
		return getClass().getSimpleName();
	}

	protected abstract Class<? extends Job> getJobClass();
	
	@Override
	public void errorEvent(Exception e) {
		// nothing to so
	}

}
