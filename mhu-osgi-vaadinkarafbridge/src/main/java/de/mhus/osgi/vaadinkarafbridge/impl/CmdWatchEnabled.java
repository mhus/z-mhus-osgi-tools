package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.vaadinbridge.BundleWatch;

@Command(scope = "vaadin", name = "watchenabled", description = "Enable / Disable bundle watch")
@Service
public class CmdWatchEnabled implements Action {

	@Argument(index=0, name="enabled", required=false, description="Enable or disable watch mode", multiValued=false)
    Boolean enabled;

	@Reference
	private BundleWatch watch;

	public Object execute() throws Exception {
		
		if (enabled == null) {
			System.out.println("Watch enabled: " + watch.isEnabled());
		} else {
			watch.setEnabled(enabled);
		}
		
		return null;
	}

}
