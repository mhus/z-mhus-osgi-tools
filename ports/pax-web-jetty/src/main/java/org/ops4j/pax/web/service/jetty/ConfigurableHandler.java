package org.ops4j.pax.web.service.jetty;

import java.util.Properties;

public interface ConfigurableHandler {

	void configure(Properties rules);
	
	void setEnabled(boolean enabled);
}
