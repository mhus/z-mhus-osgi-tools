package de.mhus.osgi.commands.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.osgi.framework.BundleContext;

import de.mhus.osgi.commands.db.TraceDataSource;

@Command(scope = "jdbc", name = "dbtrace", description = "Modify DB Trace")
public class CmdDbTrace implements Action {

	@Argument(index=0, name="source", required=true, description="Source Datasource", multiValued=false)
    String source;

	@Argument(index=1, name="option", required=true, description="enable / disable", multiValued=false)
    String option;

	@SuppressWarnings("unused")
	private BundleContext context;

	private DataSourceUtil util;

	public Object execute(CommandSession session) throws Exception {

		DataSource ds = util.getDataSource(source);
		Connection con = ds.getConnection();
		if (!(con instanceof TracedConnection)) {
			System.out.println("The source is not a trace datasource");
			return null;
		}

		TraceDataSource tds = (TraceDataSource) ((TracedConnection)con).getDataSource();
		
		if (option.equals("enable")) {
			tds.setTrace(true);
		} else
		if (option.equals("disable")) {
			tds.setTrace(false);
		} else
		if (option.equals("log")) {
			tds.setTraceFile("");
		} else
		if (option.startsWith("file:")) {
			tds.setTraceFile(option.substring(5));
		}
		System.out.println("Datasource " + source + " Trace: " + tds.isTrace() + " File: " + tds.getTraceFile() );
		
		return null;
	}

	public void setContext(BundleContext context) {
        this.context = context;
        this.util = new DataSourceUtil(context);
    }

}
