package de.mhus.osgi.services;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.logging.TracerFactory;
import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.spi.Sender;
import io.jaegertracing.thrift.internal.senders.ThriftSenderFactory;
import io.opentracing.Tracer;

public class JaegerTracerFactory implements TracerFactory {

    private Configuration config;
    private ReporterConfiguration reporterConfig;

    public JaegerTracerFactory(Configuration config, ReporterConfiguration reporterConfig) {
        this.config = config;
        this.reporterConfig = reporterConfig;
    }

    @Override
    public Tracer create() {
        if (config == null) return null;

        JaegerTracer tracer = null;
        if (MString.isSet(config.getReporter().getSenderConfiguration().getAgentHost())) {
            System.out.println("Create ThriftSender");
            Sender sender =
                    new ThriftSenderFactory().getSender(reporterConfig.getSenderConfiguration());
            if (sender == null) System.out.println("Can't create ThriftSender");
            else {
                RemoteReporter reporter = new RemoteReporter.Builder().withSender(sender).build();
                tracer = config.withReporter(reporterConfig).getTracerBuilder().withReporter(reporter).build();
            }
        }
        if (tracer == null) {
            tracer = config.getTracer();
        }
        
        return tracer;
    }

}
