package de.mhus.osgi.commands.impl;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.MFile;
import de.mhus.osgi.bundlewatch.PersistenceBundleWatchService;
import de.mhus.osgi.bundlewatch.PersistenceBundleWatchServiceIfc;

@Command(scope = "bundle", name = "persistentwatch", description = "Use bundle:watch with initial data to watches and updates bundles")
public class CmdBundleWatch implements Action {

	@Argument(index=0, name="bundle", required=false, description="(Raw) Name of the bundle to watch", multiValued=true)
    String[] bundleNames;

    @Option(name = "-r", aliases = { "--remove" }, description = "Remove the bundles from list", required = false, multiValued = false)
    boolean remove;

    @Option(name = "-c", aliases = { "--clear" }, description = "Remove all entries from list", required = false, multiValued = false)
    boolean clear;

    @Option(name = "-a", aliases = { "--activate" }, description = "Activate bundle watch", required = false, multiValued = false)
    boolean activate;
    
	private PersistenceBundleWatchServiceIfc getService() {
		BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
		ServiceReference<PersistenceBundleWatchServiceIfc> ref = bc.getServiceReference(PersistenceBundleWatchServiceIfc.class);
		PersistenceBundleWatchServiceIfc obj = bc.getService(ref);
		return obj;
	}
    
	@Override
	public Object execute(CommandSession session) throws Exception {
		
		PersistenceBundleWatchServiceIfc service = getService();
		if (service == null) {
			System.out.println("Service not found");
			return null;
		}
		
		List<String> list = service.readConfig();
		
		if (bundleNames == null) {
			for (String b : list)
				System.out.println(b);
			
			if (activate)
				service.doActivate();
			
			return null;
		}
		if (clear)
			list.clear();
		
		if (remove) {
			for (String b : bundleNames)
				list.remove(b);
		} else
			for (String b : bundleNames)
				if (!list.contains(b)) list.add(b);
		service.writeConfig(list);
		
		if (activate)
			service.doActivate();

		return null;
	}	
	

}
