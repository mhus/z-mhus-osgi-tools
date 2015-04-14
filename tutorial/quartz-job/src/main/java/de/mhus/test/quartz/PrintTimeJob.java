package de.mhus.test.quartz;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.simpl.JobAdapter;

public class PrintTimeJob extends JobAdapter {

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		System.out.println("Time: " + new Date());
	}

}
