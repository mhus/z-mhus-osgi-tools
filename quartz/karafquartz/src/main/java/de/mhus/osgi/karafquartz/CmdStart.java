package de.mhus.osgi.karafquartz;

import java.util.Date;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;
import org.osgi.framework.FrameworkUtil;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;

import de.mhus.lib.core.MDate;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.osgiquartz.Quargi;

@Command(scope = "quargi", name = "start", description = "Start the quartz system")
public class CmdStart implements Action {

	private Quargi quargi;

	@Override
	public Object execute(CommandSession session) throws Exception {

		quargi.getScheduler().start();
		System.out.println("OK");
		return null;
	}

	
	public void setQuargi(Quargi quargi) {
		this.quargi = quargi;
	}
}
