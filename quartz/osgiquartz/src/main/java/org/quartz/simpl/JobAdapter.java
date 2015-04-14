package org.quartz.simpl;

import java.util.Observable;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JobAdapter implements Job {

	protected JobExecutionContext context;
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		update(null, context);
	}

	@Override
	public void update(Observable o, Object arg) {
		
	}

}
