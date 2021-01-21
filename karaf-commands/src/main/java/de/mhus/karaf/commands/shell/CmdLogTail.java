/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.shell;

import java.io.PrintStream;
import java.util.LinkedList;

import org.apache.karaf.log.core.LogEventFormatter;
import org.apache.karaf.log.core.LogService;
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

import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.karaf.CmdInterceptor;
import de.mhus.osgi.api.karaf.CmdInterceptorUtil;

// From
// https://github.com/apache/karaf/blob/678177241a3b03181490ba4a942e99b7745ba055/log/src/main/java/org/apache/karaf/log/command/LogTail.java

@Command(
        scope = "mhus",
        name = "logtail",
        description = "Continuously display log entries. Use ctrl-c to quit this command")
@Service
public class CmdLogTail extends AbstractCmd {

    public static final int ERROR_INT = 3;
    public static final int WARN_INT = 4;
    public static final int INFO_INT = 6;
    public static final int DEBUG_INT = 7;

    @Option(
            name = "-n",
            aliases = {},
            description = "Number of entries to display",
            required = false,
            multiValued = false)
    int entries;

    @Option(
            name = "-p",
            aliases = {},
            description = "Pattern for formatting the output",
            required = false,
            multiValued = false)
    String overridenPattern;

    @Option(
            name = "--no-color",
            description = "Disable syntax coloring of log events",
            required = false,
            multiValued = false)
    boolean noColor;

    @Option(
            aliases = {"-c"},
            name = "--console-only",
            description = "Log console created messages only",
            required = false,
            multiValued = false)
    boolean consoleOnly;

    @Option(
            name = "-l",
            aliases = {"--level"},
            description = "The minimal log level to display",
            required = false,
            multiValued = false)
    @Completion(
            value = StringsCompleter.class,
            values = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR", "DEFAULT"})
    String level;

    @Argument(
            index = 0,
            name = "logger",
            description =
                    "The name of the logger. This can be ROOT, ALL, or the name of a logger specified in the org.ops4j.pax.logger.cfg file.",
            required = false,
            multiValued = false)
    String logger;

    @Reference LogService logService;

    @Reference LogEventFormatter formatter;

    @Reference Session session;

    @Reference BundleContext context;

    @Override
    public Object execute2() throws Exception {

        LogTailContainer a = (LogTailContainer) session.get("__log_tail2");
        if (a != null) {
            System.out.println("Close log tail");
            a.close();
            session.put("__log_tail2", null);
        } else {
            //	        if (entries == 0) {
            //	            entries = 50;
            //	        }
            int minLevel = getMinLevel(level);
            a =
                    new LogTailContainer(
                            session,
                            entries,
                            minLevel,
                            context,
                            logger,
                            logService,
                            formatter,
                            overridenPattern,
                            noColor,
                            consoleOnly);
            session.put("__log_tail2", a);
            if (consoleOnly) {
                LogTaggerIntercepter inter = new LogTaggerIntercepter();
                CmdInterceptorUtil.setInterceptor(session, inter);
            }
        }

        return null;
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

    private static class LogTaggerIntercepter implements CmdInterceptor {

        @Override
        public void onCmdStart(Session session) {
            LogTailContainer tail = (LogTailContainer) session.get("__log_tail2");
            if (tail == null || tail.threadFilter == null) {
                close(session);
                return;
            }
            tail.threadFilter.add(Thread.currentThread());
        }

        @Override
        public void onCmdEnd(Session session) {
            LogTailContainer tail = (LogTailContainer) session.get("__log_tail2");
            if (tail == null || tail.threadFilter == null) {
                close(session);
                return;
            }
            tail.threadFilter.remove(Thread.currentThread());
        }

        public static void close(Session session) {
            CmdInterceptorUtil.getInterceptor(session, LogTaggerIntercepter.class);
        }
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

    public static class LogTailContainer {

        ServiceTracker<LogService, LogService> tracker = null;
        private LogService logService;
        private String logger;
        private LogEventFormatter formatter;
        private String overridenPattern;
        private int entries;
        private boolean noColor;
        private LinkedList<Thread> threadFilter;
        private boolean closed;
        private Session session;

        public LogTailContainer(
                Session session,
                int entries,
                int minLevel,
                BundleContext context,
                String logger,
                LogService logService,
                LogEventFormatter formatter,
                String overridenPattern,
                boolean noColor,
                boolean threadFilter) {
            this.logService = logService;
            this.logger = logger;
            this.formatter = formatter;
            this.overridenPattern = overridenPattern;
            this.entries = entries;
            this.noColor = noColor;
            this.threadFilter = threadFilter ? new LinkedList<>() : null;

            System.out.println("Start LogTail " + (threadFilter ? "for console" : ""));

            this.session = session;
            // Do not use System.out as it may write to the wrong console depending on the thread
            // that calls our log handler
            PrintStream out = session.getConsole();
            display(out, minLevel);
            out.flush();

            PaxAppender appender = event -> printEvent(out, event, minLevel);
            tracker = new LogServiceTracker(context, LogService.class, null, appender);
            tracker.open();
        }

        public void close() {
            if (closed) return;
            closed = true;
            if (threadFilter != null) LogTaggerIntercepter.close(session);
            threadFilter = null;
            session = null;
            tracker.close();
        }

        protected void display(final PrintStream out, int minLevel) {
            if (entries <= 0) return;
            Iterable<PaxLoggingEvent> le = logService.getEvents(entries);
            for (PaxLoggingEvent event : le) {
                printEvent(out, event, minLevel);
            }
        }

        protected void printEvent(final PrintStream out, PaxLoggingEvent event) {
            if (closed) return;
            try {

                if (threadFilter != null && !threadFilter.contains(Thread.currentThread())) return;

                if ((logger != null) && (event != null) && (checkIfFromRequestedLog(event))) {
                    out.append(formatter.format(event, overridenPattern, noColor));
                } else if ((event != null) && (logger == null)) {
                    out.append(formatter.format(event, overridenPattern, noColor));
                }
            } catch (Throwable t) {
                // close
                close();
                System.out.println("close logtail by exception");
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
