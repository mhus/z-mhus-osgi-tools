package de.mhus.osgi.services;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.cfg.CfgDouble;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.service.IdentUtil;
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

	private static CfgString CFG_SAMPLER_TYPE = new CfgString(ITracer.class, "samplerType", "const");
	private static CfgDouble CFG_SAMPLER_PARAM = new CfgDouble(ITracer.class, "samplerParam", 0);
	private JaegerTracer tracer;

	@Activate
    public void doActivate(ComponentContext ctx) {
		Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv();
		if (MString.isSet(CFG_SAMPLER_TYPE.value()))
			samplerConfig.withType(CFG_SAMPLER_TYPE.value()).withParam(CFG_SAMPLER_PARAM.value());
	    Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
	    Configuration config = new Configuration(IdentUtil.getServiceIdent()).withSampler(samplerConfig).withReporter(reporterConfig);
	    tracer = config.getTracer();
	    GlobalTracer.register(tracer);
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
