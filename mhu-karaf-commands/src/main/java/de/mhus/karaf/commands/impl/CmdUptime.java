/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MPeriod;
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
		UptimeAdminIfc api = M.l(UptimeAdminIfc.class);
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
				MPeriod.getIntervalAsStringSec(record.getUptime()),
				MDate.toIsoDateTime(record.getStart()),
				record.getPid(),
				MPeriod.getIntervalAsStringSec(record.getSystemUptime())
			);
		out.print(System.out);
		return null;
	}

}
