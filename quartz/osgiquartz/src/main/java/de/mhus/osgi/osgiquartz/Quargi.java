package de.mhus.osgi.osgiquartz;

import org.quartz.Scheduler;

public interface Quargi {

	Scheduler getScheduler();
	
	public void resetSystem();

	QuargiJob[] getJobs();
	
}
