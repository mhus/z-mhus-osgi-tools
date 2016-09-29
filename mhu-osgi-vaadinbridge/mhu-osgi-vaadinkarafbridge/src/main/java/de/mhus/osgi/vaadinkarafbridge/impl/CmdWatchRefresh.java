package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.vaadinbridge.BundleWatch;

@Command(scope = "vaadin", name = "watchrefresh", description = "Full-Refresh automatic bundle watch")
@Service
public class CmdWatchRefresh implements Action {

	private BundleWatch watch;

	public Object execute() throws Exception {
		
		watch.refreshAll();
		
		return null;
	}

	public void setBundleWatch(BundleWatch watch) {
		this.watch = watch;
	}

}
