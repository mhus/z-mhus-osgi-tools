package org.quartz.simpl;

import java.util.Observable;
import java.util.Observer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ObserverJob extends JobAdapter {

	private Observer runnable;

	public ObserverJob(Observer runnable) {
		this.runnable = runnable;
	}

	@Override
	public void update(Observable o, Object arg) {
		runnable.update(o, arg);
	}

}
