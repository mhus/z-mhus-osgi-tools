package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.osgi.vaadinbridge.BundleWatch;

@Command(scope = "vaadin", name = "watchenabled", description = "Enable / Disable bundle watch")
public class CmdWatchEnabled implements Action {

	@Argument(index=0, name="enabled", required=false, description="Enable or disable watch mode", multiValued=false)
    Boolean enabled;
	private BundleWatch watch;

	public Object execute(CommandSession session) throws Exception {
		
		if (enabled == null) {
			System.out.println("Watch enabled: " + watch.isEnabled());
		} else {
			watch.setEnabled(enabled);
		}
		
		return null;
	}

	public void setBundleWatch(BundleWatch watch) {
		this.watch = watch;
	}

}
