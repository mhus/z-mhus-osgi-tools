package de.mhus.karaf.vaadinkarafbridge.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceRemove", description = "Remove a resource provider")
public class CmdVaadinResourceRemove implements Action {

	@Argument(index=0, name="bundle", required=true, description="Bundle Name", multiValued=false)
    String bundle;
	private VaadinConfigurableResourceProviderAdmin provider;

	public Object execute(CommandSession session) throws Exception {
		
		provider.removeResource(bundle);
		
		return null;
	}

	public void setResourceProvider(VaadinConfigurableResourceProviderAdmin provider) {
		this.provider = provider;
	}

}
