package de.mhus.karaf.commands.shell;


import java.io.PrintStream;

import org.apache.karaf.log.core.LogEventFormatter;
import org.apache.karaf.log.core.LogService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

// From https://github.com/apache/karaf/blob/678177241a3b03181490ba4a942e99b7745ba055/log/src/main/java/org/apache/karaf/log/command/LogTail.java

@Command(scope = "mhus", name = "logtail", description = "Continuously display log entries. Use ctrl-c to quit this command")
@Service
public class CmdLogTail implements Action {
	
    public final static int ERROR_INT = 3;
    public final static int WARN_INT  = 4;
    public final static int INFO_INT  = 6;
    public final static int DEBUG_INT = 7;

    @Option(name = "-n", aliases = {}, description="Number of entries to display", required = false, multiValued = false)
    int entries;

    @Option(name = "-p", aliases = {}, description="Pattern for formatting the output", required = false, multiValued = false)
    String overridenPattern;

    @Option(name = "--no-color", description="Disable syntax coloring of log events", required = false, multiValued = false)
    boolean noColor;

    @Option(aliases = {"-c"}, name = "--console-only", description="Log console created messages only", required = false, multiValued = false)
    boolean consoleOnly;
    
    @Option(name = "-l", aliases = { "--level" }, description = "The minimal log level to display", required = false, multiValued = false)
    @Completion(value = StringsCompleter.class, values = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "DEFAULT" })
    String level;

    @Argument(index = 0, name = "logger", description = "The name of the logger. This can be ROOT, ALL, or the name of a logger specified in the org.ops4j.pax.logger.cfg file.", required = false, multiValued = false)
    String logger;

    @Reference
    LogService logService;

    @Reference
    LogEventFormatter formatter;


    @Reference
    Session session;

    @Reference
    BundleContext context;

    @Override
    public Object execute() throws Exception {
    	
    	LogTailContainer a = (LogTailContainer) session.get("__log_tail2");
		if (a != null) {
			System.out.println("Close log tail");
			a.close();
			session.put("__log_tail2",null);
		} else {
	        if (entries == 0) {
	            entries = 50;
	        }
	        int minLevel = getMinLevel(level);
			a = new LogTailContainer(session,entries,minLevel,context,logger,logService,formatter,overridenPattern, noColor, consoleOnly ? Thread.currentThread() : null);
			session.put("__log_tail2",a);
		}

        return null;
    }
    
    protected static int getMinLevel(String levelSt) {
        int minLevel = Integer.MAX_VALUE;
        if (levelSt != null) {
            switch (levelSt.toLowerCase()) {
            case "debug": minLevel = DEBUG_INT; break;
            case "info":  minLevel = INFO_INT; break;
            case "warn":  minLevel = WARN_INT; break;
            case "error": minLevel = ERROR_INT; break;
            }
        }
        return minLevel;
    }
    
    
    /**
     * Track LogService dynamically so we can react when the log core bundle stops even while we block for the tail
     */
    private static final class LogServiceTracker extends ServiceTracker<LogService, LogService> {

        private final static String SSHD_LOGGER = "org.apache.sshd";

        private final PaxAppender appender;

        private String sshdLoggerLevel;
    
        private LogServiceTracker(BundleContext context, Class<LogService> clazz,
                                  ServiceTrackerCustomizer<LogService, LogService> customizer,
                                  PaxAppender appender) {
            super(context, clazz, customizer);
            this.appender = appender;
        }
    
        @Override
        public LogService addingService(ServiceReference<LogService> reference) {
            LogService service = super.addingService(reference);
            sshdLoggerLevel = service.getLevel(SSHD_LOGGER).get(SSHD_LOGGER);
            service.setLevel(SSHD_LOGGER, "ERROR");
            service.addAppender(appender);
            return service;
        }
    
        @Override
        public void removedService(ServiceReference<LogService> reference, LogService service) {
            if (sshdLoggerLevel != null) {
                service.setLevel(SSHD_LOGGER, sshdLoggerLevel);
            }
            service.removeAppender(appender);
            //stopTail();
        }
    }
    
    public static class LogTailContainer {

    	ServiceTracker<LogService, LogService> tracker = null;
		private LogService logService;
		private String logger;
		private LogEventFormatter formatter;
		private String overridenPattern;
		private int entries;
		private boolean noColor;
		private Thread threadFilter;
    	
		public LogTailContainer(Session session, int entries, int minLevel, BundleContext context, String logger, LogService logService, LogEventFormatter formatter, String overridenPattern, boolean noColor, Thread threadFilter) {
			this.logService = logService;
			this.logger = logger;
			this.formatter = formatter;
			this.overridenPattern = overridenPattern;
			this.entries = entries;
			this.noColor = noColor;
			this.threadFilter = threadFilter;
			
	        // Do not use System.out as it may write to the wrong console depending on the thread that calls our log handler
	        PrintStream out = session.getConsole();
	        display(out, minLevel);
	        out.flush();

	        PaxAppender appender = event -> printEvent(out, event, minLevel);
	        tracker = new LogServiceTracker(context, LogService.class, null, appender);
	        tracker.open();

		}

		public void close() {
            tracker.close();
		}

	    protected void display(final PrintStream out, int minLevel) {
	        Iterable<PaxLoggingEvent> le = logService.getEvents(entries == 0 ? Integer.MAX_VALUE : entries);
	        for (PaxLoggingEvent event : le) {
	            printEvent(out, event, minLevel);
	        }
	    }
	    
	    protected void printEvent(final PrintStream out, PaxLoggingEvent event) {
	    	try {
	    		
	    		if (threadFilter != null && threadFilter != Thread.currentThread())
	    			return;
	    		
		        if ((logger != null) &&
		                (event != null) &&
		                (checkIfFromRequestedLog(event))) {
		            out.append(formatter.format(event, overridenPattern, noColor));
		        } else if ((event != null) && (logger == null)) {
		            out.append(formatter.format(event, overridenPattern, noColor));
		        }
	    	} catch (Throwable t) {
	    		// close
	    		close();
	    		System.out.println("Close by exception");
	    		t.printStackTrace();
	    	}
	    	
	    }

	    protected boolean checkIfFromRequestedLog(PaxLoggingEvent event) {
	    	return event.getLoggerName().contains(logger);
	    }
	    

	    protected void printEvent(PrintStream out, PaxLoggingEvent event, int minLevel) {
	        try {
	            if (event != null) {
	                int sl = event.getLevel().getSyslogEquivalent();
	                if (sl <= minLevel) {
	                    printEvent(out, event);
	                }
	            }
	        } catch (NoClassDefFoundError e) {
	            // KARAF-3350: Ignore NoClassDefFoundError exceptions
	            // Those exceptions may happen if the underlying pax-logging service
	            // bundle has been refreshed somehow.
	        }
	    }

    }
}