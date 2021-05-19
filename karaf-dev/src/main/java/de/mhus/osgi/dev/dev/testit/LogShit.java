package de.mhus.osgi.dev.dev.testit;

import java.io.PrintStream;

import org.apache.karaf.log.core.LogService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import de.mhus.osgi.api.MOsgi;

public class LogShit implements ShitIfc {

    public static final int ERROR_INT = 3;
    public static final int WARN_INT = 4;
    public static final int INFO_INT = 6;
    public static final int DEBUG_INT = 7;
    private static LogServiceTracker tracker;
    private boolean closed;

    @Override
    public void printUsage() {
        System.out.println("tail <level>");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {

        if (cmd.equals("tail")) {
            if (tracker != null) {
                System.out.println("Already running");
                return null;
            }
            int minLevel = getMinLevel(parameters[0]);
            PaxAppender appender = event -> printEvent(base.getSession().getConsole(), event, minLevel);
            tracker = new LogServiceTracker(MOsgi.getBundleContext(LogShit.class), LogService.class, null, appender);
            closed = false;
            tracker.open();
            
            try {
                while(true)
                    Thread.sleep(100000);
            } catch(InterruptedException e) {}

            tracker.close();
            tracker = null;
        }
        return null;
    }

    protected void printEvent(PrintStream out, PaxLoggingEvent event, int minLevel) {
        if (closed) return;
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
    
    protected void printEvent(final PrintStream out, PaxLoggingEvent event) {
        try {
            out.println("{");
            out.println("  name :" + event.getLoggerName());
            out.println("  time :" + event.getTimeStamp());
            out.println("  level:" + event.getLevel());
            out.println("  msg  :" + event.getMessage());
            out.println("}");
        } catch (Throwable t) {
            // close
            closed = true;
            System.out.println("close logtail by exception");
            t.printStackTrace();
        }
    }

    protected static int getMinLevel(String levelSt) {
        int minLevel = Integer.MAX_VALUE;
        if (levelSt != null) {
            switch (levelSt.toLowerCase()) {
                case "debug":
                    minLevel = DEBUG_INT;
                    break;
                case "info":
                    minLevel = INFO_INT;
                    break;
                case "warn":
                    minLevel = WARN_INT;
                    break;
                case "error":
                    minLevel = ERROR_INT;
                    break;
            }
        }
        return minLevel;
    }

    /**
     * Track LogService dynamically so we can react when the log core bundle stops even while we
     * block for the tail
     */
    private static final class LogServiceTracker extends ServiceTracker<LogService, LogService> {

        private static final String SSHD_LOGGER = "org.apache.sshd";

        private final PaxAppender appender;

        private String sshdLoggerLevel;

        private LogServiceTracker(
                BundleContext context,
                Class<LogService> clazz,
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
            // stopTail();
        }
    }

}
