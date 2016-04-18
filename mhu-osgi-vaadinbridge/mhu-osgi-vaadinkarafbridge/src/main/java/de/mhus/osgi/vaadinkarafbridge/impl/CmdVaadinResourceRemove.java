package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceRemove", description = "Remove a resource provider")
@Service
public class CmdVaadinResourceRemove implements Action {

	@Argument(index=0, name="bundle", required=true, description="Bundle Name", multiValued=false)
    String bundle;
	private VaadinConfigurableResourceProviderAdmin provider;

	public Object execute() throws Exception {
		
		provider.removeResource(bundle);
		
		return null;
	}

	public void setResourceProvider(VaadinConfigurableResourceProviderAdmin provider) {
		this.provider = provider;
	}

}
