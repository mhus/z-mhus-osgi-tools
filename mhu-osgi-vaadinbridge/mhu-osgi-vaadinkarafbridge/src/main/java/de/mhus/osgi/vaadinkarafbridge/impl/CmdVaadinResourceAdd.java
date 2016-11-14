package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceAdd", description = "Add a resource provider")
@Service
public class CmdVaadinResourceAdd implements Action {

	@Argument(index=0, name="bundle", required=true, description="Bundle Name", multiValued=false)
    String bundle;

	@Argument(index=1, name="pathes", required=true, description="Pathes", multiValued=true)
    String[] pathes;

	@Reference
	private VaadinConfigurableResourceProviderAdmin provider;

	public Object execute() throws Exception {
		
//		System.out.println("ADD: " + bundle + ":" + pathes);
		provider.addResource(bundle, pathes);
		
		return null;
	}

}
