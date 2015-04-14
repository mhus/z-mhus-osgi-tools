package de.mhus.osgi.osgiquartz.impl;

import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import de.mhus.osgi.osgiquartz.Quargi;
import de.mhus.osgi.osgiquartz.QuargiJob;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component(immediate=true,name="Quargi",provide=Quargi.class)
public class OsgiQuartzImpl implements Quargi {

	private static Logger log = Logger.getLogger(Quargi.class.getName());
	private StdSchedulerFactory sf;
	private Scheduler scheduler;
	private ServiceTracker<QuargiJob, QuargiJob> tracker;
	private BundleContext context;

	@Activate
	public void doActivate(ComponentContext ctx) {
		
		sf = new StdSchedulerFactory();
		try {
			scheduler = sf.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

		if (ctx != null)
			context = ctx.getBundleContext();
		tracker = new ServiceTracker<QuargiJob,QuargiJob>(context, QuargiJob.class, new WSCustomizer() );
		tracker.open();

	}
	
	
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		log.info("*** shutdown ***");
		
		tracker.close();
		
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	private class WSCustomizer implements ServiceTrackerCustomizer<QuargiJob, QuargiJob> {

		@Override
		public QuargiJob addingService(ServiceReference<QuargiJob> reference) {
			
			QuargiJob job = context.getService(reference);
			try {
				JobDetail j = job.getJob();
				Trigger t = job.getTrigger();
				
				scheduler.scheduleJob(j, t);
			} catch (Exception e) {
				e.printStackTrace();
				job.errorEvent(e);
				return null;
			}
			return job;
		}

		@Override
		public void modifiedService(ServiceReference<QuargiJob> reference,
				QuargiJob service) {
			
			QuargiJob job = null;
			try {
				job = context.getService(reference);
				JobDetail j = job.getJob();
				Trigger t = job.getTrigger();
				try {
					scheduler.deleteJob(j.getKey());
				} catch (SchedulerException e) {
					e.printStackTrace();
					job.errorEvent(e);
				}
				scheduler.scheduleJob(j, t);
			} catch (Exception e) {
				e.printStackTrace();
				job.errorEvent(e);
			}
			
		}

		@Override
		public void removedService(ServiceReference<QuargiJob> reference,
				QuargiJob service) {
			QuargiJob job = context.getService(reference);
			try {
				JobDetail j = job.getJob();
				scheduler.deleteJob(j.getKey());
			} catch (Exception e) {
				e.printStackTrace();
				job.errorEvent(e);
			}
		}
		
	}

	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public void resetSystem() {
		doDeactivate(null);
		doActivate(null);
	}
	
	@Override
	public QuargiJob[] getJobs() {
		return tracker.getServices(new QuargiJob[0]);
	}

}
