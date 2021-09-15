package de.mhus.osgi.services;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.logging.TracerProxy;
import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.spi.Sender;
import io.jaegertracing.thrift.internal.senders.ThriftSenderFactory;
import io.opentracing.Tracer;

public class JaegerTracerProxy extends TracerProxy {

    private Configuration config;
    private ReporterConfiguration reporterConfig;

    public JaegerTracerProxy(Configuration config, ReporterConfiguration reporterConfig) {
        this.config = config;
        this.reporterConfig = reporterConfig;
        reset();
    }

    @Override
    protected Tracer create() {
        if (config == null) return null;

        JaegerTracer tracer = null;
        if (MString.isSet(config.getReporter().getSenderConfiguration().getAgentHost())) {
            log().i("Create ThriftSender");
            Sender sender =
                    new ThriftSenderFactory().getSender(reporterConfig.getSenderConfiguration());
            if (sender == null) log().i("Can't create ThriftSender");
            else {
                RemoteReporter reporter = new RemoteReporter.Builder().withSender(sender).build();
                tracer = config.getTracerBuilder().withReporter(reporter).build();
            }
        }
        if (tracer == null) {
            tracer = config.getTracer();
        }
        
        return tracer;
    }

    public void reset(Configuration config, ReporterConfiguration reporterConfig) {
        this.config = config;
        this.reporterConfig = reporterConfig;
        reset();
    }

}
