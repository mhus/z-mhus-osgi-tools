package de.mhus.osgi.commands.impl;

import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@Command(scope = "bundle", name = "upgrade", description = "Upgrade bundle version")
@Service
public class CmdBundleUpgrade implements Action {

    @Reference
	private BundleContext context;

	@Argument(index=0, name="bundle", required=true, description="bundle filter (regex)", multiValued=false)
    String bundleFilter;

	@Argument(index=1, name="version", required=false, description="bundle version to install", multiValued=false)
    String bundleVersion;

    @Option(name = "-y", aliases = { "--yes" }, description = "Say yes", required = false, multiValued = false)
    boolean useYes;

    @Option(name = "-n", aliases = { "--no" }, description = "Say no", required = false, multiValued = false)
    boolean useNo;
    
    @Option(name = "-i", aliases = { "--install" }, description = "Install only", required = false, multiValued = false)
    boolean installOnly;
    
    @Option(name = "-u", aliases = { "--notuninstall" }, description = "Do not uninstall bundles", required = false, multiValued = false)
    boolean notuninstall;
    
    @Reference
    private Session session;

	@Override
	public Object execute() throws Exception {
		
		LinkedList<Cont> list = new LinkedList<>();
		for (Bundle b : context.getBundles())
			if (b.getSymbolicName().matches(bundleFilter)) {
				list.add(new Cont(b));
			}

		
		return null;
	}
	
	class Cont {

		private Bundle bundle;

		public Cont(Bundle b) {
			this.bundle = b;
		}
		
	}

}
