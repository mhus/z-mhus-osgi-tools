package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceAdd", description = "Add a resource provider")
public class CmdVaadinResourceAdd implements Action {

	@Argument(index=0, name="bundle", required=true, description="Bundle Name", multiValued=false)
    String bundle;

	@Argument(index=1, name="pathes", required=true, description="Pathes", multiValued=true)
    String[] pathes;

	private VaadinConfigurableResourceProviderAdmin provider;

	public Object execute(CommandSession session) throws Exception {
		
//		System.out.println("ADD: " + bundle + ":" + pathes);
		provider.addResource(bundle, pathes);
		
		return null;
	}

	public void setResourceProvider(VaadinConfigurableResourceProviderAdmin provider) {
		this.provider = provider;
	}
}
