package de.mhus.test.quartz;

import org.quartz.Job;

import de.mhus.osgi.osgiquartz.IntervalJob;
import de.mhus.osgi.osgiquartz.QuargiJob;
import aQute.bnd.annotation.component.Component;

@Component(provide=QuargiJob.class,name="DemoJobDefinition",immediate=true)
public class DemoJobDefinition extends IntervalJob {

	@Override
	protected int getIntervalInSeconds() {
		return 30;
	}

	@Override
	protected Class<? extends Job> findJobClass() {
		return PrintTimeJob.class;
	}

}
