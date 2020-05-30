package de.mhus.osgi.api.karaf;

import java.util.function.Consumer;

import org.apache.karaf.log.core.LogService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class LogServiceTracker extends ServiceTracker<LogService, LogService> {

    public static final int ERROR_INT = 3;
    public static final int WARN_INT  = 4;
    public static final int INFO_INT  = 6;
    public static final int DEBUG_INT = 7;
    public static final int ALL_INT   = 100;
    
    public enum LOG_LEVEL { 
        ERROR {
            @Override
            public int toInt() {
                return ERROR_INT;
                }
            }, 
        WARN {
            @Override
            public int toInt() {
                return WARN_INT;
                }
            }, 
        INFO {
            @Override
            public int toInt() {
                return INFO_INT;
                }
            }, 
        DEBUG {
            @Override
            public int toInt() {
                return DEBUG_INT;
                }
            }, 
        ALL {
            @Override
            public int toInt() {
                return ALL_INT;
                }
            };

            public int toInt() {
                return 0;
            }
    }
	
    private static final String SSHD_LOGGER = "org.apache.sshd";

    private final PaxAppender appender;

    private String sshdLoggerLevel;

	private Consumer<PaxLoggingEvent> consumer;

    public LogServiceTracker(
            BundleContext context,
            Consumer<PaxLoggingEvent> consumer
            ) {
        super(context, LogService.class, null);
        this.consumer = consumer;
        this.appender = event -> printEvent(event);
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
    }
    
    private void printEvent(PaxLoggingEvent event) {
        // scan log
        try {
            if (event != null) {
            	consumer.accept(event);
            }
        } catch (NoClassDefFoundError e) {
            // KARAF-3350: Ignore NoClassDefFoundError exceptions
            // Those exceptions may happen if the underlying pax-logging service
            // bundle has been refreshed somehow.
        }
    }

}
