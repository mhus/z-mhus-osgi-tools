package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "debug", description = "Enable / Disable debug mode")
@Service
public class CmdVaadinDebug implements Action {

	@Argument(index=0, name="debug", required=true, description="Debug Mode", multiValued=false)
    boolean debug;
	
	@Reference
	private VaadinConfigurableResourceProviderAdmin provider;

	public Object execute() throws Exception {
		
		provider.setDebug(debug);
		
		return null;
	}

}
