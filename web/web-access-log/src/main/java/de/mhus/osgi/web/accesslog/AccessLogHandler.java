package de.mhus.osgi.web.accesslog;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.ops4j.pax.web.service.jetty.CentralCallContext;
import org.ops4j.pax.web.service.jetty.CentralRequestHandler;
import org.ops4j.pax.web.service.jetty.ConfigurableHandler;

import aQute.bnd.annotation.component.Component;

@Component(immediate=true)
public class AccessLogHandler implements CentralRequestHandler, ConfigurableHandler {

	public static final String TIME_KEY = "web-access-log-time";
	private static Logger log = Logger.getLogger("web-access-log");
	
	private boolean enabled = true;
	
	@Override
	public boolean doHandleBefore(CentralCallContext context) throws IOException, ServletException {
		log.info("Request," + context.getHost() + "," + context.getTarget());
		context.setAttribute(TIME_KEY, System.currentTimeMillis());
		context.setResponse(new StatusExposingServletResponse(context.getResponse()));
		return false;
	}

	@Override
	public boolean doHandleAfter(CentralCallContext context)
			throws IOException, ServletException {
		
		long cur = System.currentTimeMillis();
		Long start = (Long) context.getAttribute(TIME_KEY);
		long time = 0;
		if (start != null) {
			time = cur - start;
		}
		int rc = 0;
		if (context.getResponse() instanceof StatusExposingServletResponse) {
			rc = ((StatusExposingServletResponse)context.getResponse()).getStatus();
		}
		if (rc != 0 && rc != 200 || time > 1000)
			log.info("Warn," + context.getBaseRequest().getHeader("host") + "," + context.getTarget() + "," + time + "," + rc);
		return false;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public double getSortHint() {
		return -10;
	}

	@Override
	public void configure(Properties rules) {
		
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
