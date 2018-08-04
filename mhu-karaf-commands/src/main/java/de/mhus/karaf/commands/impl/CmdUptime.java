package de.mhus.karaf.commands.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.services.uptime.UptimeAdminIfc;
import de.mhus.osgi.services.uptime.UptimeRecord;

@Command(scope = "mhus", name = "uptime", description = "Information about runtime")
@Service
public class CmdUptime implements Action {

    @Option(name = "-u", aliases = { "--uptime" }, description = "Order by uptime descend", required = false, multiValued = false)
    boolean orderUptime;

	@Override
	public Object execute() throws Exception {
		
		ConsoleTable out = new ConsoleTable(false);
		out.setHeaderValues("Status","Runtime","Start","Pid","System");
		UptimeAdminIfc api = MApi.lookup(UptimeAdminIfc.class);
		List<UptimeRecord> records = api.getRecords();
		if (records == null) {
			System.out.println("Records are not available");
			return null;
		}
		if (orderUptime)
			Collections.sort(records, new Comparator<UptimeRecord>() {

				@Override
				public int compare(UptimeRecord o1, UptimeRecord o2) {
					return -Long.compare(o1.getUptime(), o2.getUptime());
				}
			});
		
		for (UptimeRecord record : records)
			out.addRowValues(
				record.getStatus(), 
				MTimeInterval.getIntervalAsStringSec(record.getUptime()),
				MDate.toIsoDateTime(record.getStart()),
				record.getPid(),
				MTimeInterval.getIntervalAsStringSec(record.getSystemUptime())
			);
		out.print(System.out);
		return null;
	}

}
