package de.mhus.osgi.services;

import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.cfg.CfgDouble;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.service.IdentUtil;
import de.mhus.osgi.api.karaf.LogServiceTracker;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.JaegerTracer.SpanBuilder;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;

// https://www.scalyr.com/blog/jaeger-tracing-tutorial/

@Component
public class JaegerTracerService extends MLog implements ITracer {

	private CfgString CFG_SAMPLER_TYPE = new CfgString(JaegerTracerService.class, "samplerType", "const");
	private CfgDouble CFG_SAMPLER_PARAM = new CfgDouble(JaegerTracerService.class, "samplerParam", 0);
	
	private JaegerTracer tracer;
	private LogServiceTracker tracker;
	
	private static String[] JAEGER_ENV = new String[] {
			"JAEGER_SAMPLER_TYPE",
			"JAEGER_SAMPLER_PARAM",
			"JAEGER_SAMPLER_MANAGER_HOST_PORT",
			"JAEGER_REPORTER_LOG_SPANS",
			"JAEGER_AGENT_HOST",
			"JAEGER_AGENT_PORT",
			"JAEGER_REPORTER_FLUSH_INTERVAL",
			"JAEGER_REPORTER_MAX_QUEUE_SIZE",
			"JAEGER_SERVICE_NAME"
	};
	
	@Activate
    public void doActivate(ComponentContext ctx) {
	    
		update();
		
	    tracker = new LogServiceTracker(ctx.getBundleContext(), e -> logEvent(e));
	    tracker.open();
	}

	@Modified
    public void doModified(ComponentContext ctx) {
		update();
	}
	
    private synchronized void update() {
    	log().i("Update jaeger tracer");
    	
    	// prepare env
    	IConfig cfg = MApi.getCfg(JaegerTracerService.class);
    	for (String key : JAEGER_ENV) {
    		if (cfg != null && cfg.containsKey(key))
    			System.setProperty(key, cfg.getString(key, null));
    		else
			if (System.getenv(key) != null)
    			System.setProperty(key, System.getenv(key));
    	}
    	
		Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv();
		if (MString.isSet(CFG_SAMPLER_TYPE.value()))
			samplerConfig.withType(CFG_SAMPLER_TYPE.value()).withParam(CFG_SAMPLER_PARAM.value());
	    Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
	    Configuration config = new Configuration(IdentUtil.getServiceIdent()).withSampler(samplerConfig).withReporter(reporterConfig);

	    if (tracer != null) {
	    	tracer.close();
	    	tracer = null;
	    }
	    tracer = config.getTracer();
	    GlobalTracer.register(tracer);

	}

	private void logEvent(PaxLoggingEvent e) {
    	Span span = current();
    	if (span == null) return;
    	if (e.getLevel().toInt() > LogServiceTracker.DEBUG_INT) return;
    	Map<String, String> fields = new HashMap<>();
    	fields.put("level", e.getLevel().toString());
    	fields.put("message", e.getMessage());
    	fields.put("logger", e.getLoggerName());
    	fields.put("thread", e.getThreadName());
    	fields.put("location", e.getLocationInformation().toString());
    	fields.put("ident", IdentUtil.getServerIdent());
		span.log(fields );
	}

	@Deactivate
    public void deactivate(ComponentContext ctx) {
        if (tracker != null)
            tracker.close();
    }
	
	@Override
	public Scope start(String name, boolean active, String... tagPairs) {
		
		// cleanup all before start
		while (tracer.scopeManager().active() != null)
			tracer.scopeManager().active().close();
		
		SpanBuilder span = tracer.buildSpan(name);
		for (int i = 0; i < tagPairs.length-1; i=i+2)
			span.withTag(tagPairs[i],tagPairs[i+1]);
		Scope scope = span.startActive(true);
		if (active)
			Tags.SAMPLING_PRIORITY.set(scope.span(), 1);
		return scope;
	}

	@Override
	public Scope enter(String name, String... tagPairs) {
		SpanBuilder span = tracer.buildSpan(name);
		for (int i = 0; i < tagPairs.length-1; i=i+2)
			span.withTag(tagPairs[i],tagPairs[i+1]);
		Scope scope = span.startActive(true);
		return scope;
	}


	// https://opentracing.io/guides/java/inject-extract/
	@Override
	public void inject(String type, Object object) {
		
	}


	@Override
	public Scope extract(String type, Object object) {
		return null;
	}


	@Override
	public Span current() {
		return tracer.activeSpan();
	}

}
