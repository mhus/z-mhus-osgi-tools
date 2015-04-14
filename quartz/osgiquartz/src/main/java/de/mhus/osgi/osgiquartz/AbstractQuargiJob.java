package de.mhus.osgi.osgiquartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;

public abstract class AbstractQuargiJob implements QuargiJob {

	private JobDetail job;
	private Trigger trigger;
	private String jobClass;

	@Override
	public JobDetail getJob() throws ClassNotFoundException {
		if (job == null)
			job = createJob();
		return job;
	}

	@Override
	public Trigger getTrigger() throws ClassNotFoundException {
		if (trigger == null)
			trigger = createTrigger();
		return trigger;
	}

	protected abstract Trigger createTrigger() throws ClassNotFoundException;
	
	protected JobDetail createJob() throws ClassNotFoundException {
		return JobBuilder.newJob(findJobClass()).withIdentity(getJobName(),getJobGroup()).withDescription(getJobDescription()).build();
	}
	
	protected String getJobGroup() throws ClassNotFoundException {
		return findJobClass().getPackage().getName();
	}

	protected String getJobDescription() {
		return getClass().getCanonicalName();
	}

	protected String getJobName() {
		return getClass().getSimpleName();
	}

//	protected abstract Class<? extends Job> findJobClass() throws ClassNotFoundException;
	
	@Override
	public void errorEvent(Exception e) {
		// nothing to so
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends Job> findJobClass() throws ClassNotFoundException {
		return (Class<? extends Job>) getClass().getClassLoader().loadClass(jobClass);
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}


}
