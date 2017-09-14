package de.mhus.osgi.sop.api.util;

import java.io.File;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.logging.FileLogger;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;

// TODO implement asynchrony logging
public class SopFileLogger extends FileLogger {

	private static CfgString logDir = new CfgString(SopFileLogger.class, "logDirectory", null) {
		@Override
		protected void onPreUpdate(String newValue) {
			if (newValue == null) return;
					new File(newValue).mkdirs();
		}
	};
	private String logName;
	
	public SopFileLogger(String name, String logName) {
		super(name, null );
		this.logName = logName;
	}

	@Override
	protected String getInfo() {
		StringBuffer out = new StringBuffer();
		out.append(Thread.currentThread().getId()).append(',');
		out.append(Thread.currentThread().getName()).append(',');
		
		AaaContext context = MApi.lookup(AccessApi.class).getCurrentOrGuest();
		out.append(context.getAccountId());
		
		return out.toString();
	}

	@Override
    protected void prepare(StringBuffer sb) {
	}

	@Override
	protected void doUpdateFile() {
		file = logDir.value() == null ? null : new File(logDir.value(), logName + ".log");
	}

}
