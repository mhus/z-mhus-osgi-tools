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
                tracer =
                        config.withReporter(reporterConfig)
                                .getTracerBuilder()
                                .withReporter(reporter)
                                .build();
            }
        }
        if (tracer == null) {
            tracer = config.getTracer();
        }

        return tracer;
    }
}
