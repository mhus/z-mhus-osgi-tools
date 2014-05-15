package de.mhus.karaf.vaadinkarafbridge.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "debug", description = "Enable / Disable debug mode")
public class CmdVaadinDebug implements Action {

	@Argument(index=0, name="debug", required=true, description="Debug Mode", multiValued=false)
    boolean debug;
	private VaadinConfigurableResourceProviderAdmin provider;

	public Object execute(CommandSession session) throws Exception {
		
		provider.setDebug(debug);
		
		return null;
	}

	public void setResourceProvider(VaadinConfigurableResourceProviderAdmin provider) {
		this.provider = provider;
	}

}
