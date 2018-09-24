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
package de.mhus.osgi.services.uptime;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.cfg.CfgInt;
import de.mhus.lib.core.system.IApi;

@Component(immediate=true)
public class UptimeAdminImpl extends MLog implements UptimeAdminIfc {

	private static final CfgInt MAX = new CfgInt(UptimeAdminIfc.class, "max", 10000);
	private List<MutableRecord> db;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		long jStart = MSystem.getJvmStartTime();
		db = loadDb();
		if (db == null) return;
		
		if (db.size() == 0 || db.get(db.size()-1).getStart() != jStart) {
			// new Entry
			db.add(new MutableRecord(jStart));
		} else {
			// set last to current
			db.get(db.size()-1).setCurrent();
		}
		saveDb(db);
	}
	
	
	private void saveDb(List<MutableRecord> db) {
		File file = MApi.getFile(IApi.SCOPE.DATA, "uptime.db");
		List<String> lines = new LinkedList<>();
		for (MutableRecord record : db)
			lines.add(record.toLine());
		try {
			MFile.writeLines(file, lines, false);
		} catch (IOException e) {
			log().e(e);
		}
	}


	private List<MutableRecord> loadDb() {
		File file = MApi.getFile(IApi.SCOPE.DATA, "uptime.db");
		List<MutableRecord> out = new LinkedList<>();
		if (!file.exists()) 
			return out;
		try {
			List<String> lines = MFile.readLines(file, true);
			for (String line : lines)
				out.add(new MutableRecord(line));
			while (out.size() > MAX.value())
				out.remove(0);
			return out;
		} catch (Throwable t) {
			log().e(t);
		}
		return null;
	}

	@Override
	public List<UptimeRecord> getRecords() {
		if (db == null) return null;
		return new LinkedList<UptimeRecord>(db);
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		long jStart = MSystem.getJvmStartTime();
		if (db == null) return;
		if (db.size() == 0 || db.get(db.size()-1).getStart() != jStart) {
			// new Entry - should not happen
			db.add(new MutableRecord(jStart));
			log().w("new record at shut down");
		}
		// set last to closed
		db.get(db.size()-1).setCloses();
		saveDb(db);
	}

	private static class MutableRecord implements UptimeRecord {

		private long start;
		private int status;
		private long stop;
		private String pid;
		private long sysUptime;

		public MutableRecord(long start) {
			this.start = start;
			this.status = STATUS.CURRENT.ordinal();
			pid = MSystem.getPid();
			sysUptime = MSystem.getSystemUptime();
		}

		public MutableRecord(String line) {
			String[] parts = line.split(";");
			start = M.c(parts[0], 0l);
			stop = M.c(parts[1], 0l);
			status = M.c(parts[2], 0);
			pid = parts[3].replace(';', ',');
			sysUptime = M.c(parts[4], 0l);
			
			if (status == STATUS.CURRENT.ordinal())
				status = STATUS.UNKNOWN.ordinal();
		}

		public void setCloses() {
			status = STATUS.CLOSED.ordinal();
			sysUptime = MSystem.getSystemUptime();
			pid = MSystem.getPid();
			stop = System.currentTimeMillis();
		}

		public String toLine() {
			return start + ";" + stop + ";" + status + ";" + pid + ";" + sysUptime;
		}

		public void setCurrent() {
			status = STATUS.CURRENT.ordinal();
			stop = System.currentTimeMillis();
			sysUptime = MSystem.getSystemUptime();
			pid = MSystem.getPid();
		}

		@Override
		public long getStart() {
			return start;
		}

		@Override
		public long getStop() {
			return stop;
		}

		@Override
		public STATUS getStatus() {
			return STATUS.values()[status];
		}

		@Override
		public String getPid() {
			return pid;
		}
		
		@Override
		public long getSystemUptime() {
			if (status == STATUS.CURRENT.ordinal())
				return MSystem.getSystemUptime();
			return sysUptime;
		}
		
		@Override
		public long getUptime() {
			if (status == STATUS.CURRENT.ordinal())
				return System.currentTimeMillis() - start;
			return stop - start;
		}
		
	}
}
