package de.mhus.osgi.commands.bundle;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import de.mhus.lib.core.MFile;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

@Component(immediate=true,name="PersistenceBundleWatchService",provide=PersistenceBundleWatchServiceIfc.class)
public class PersistenceBundleWatchService implements PersistenceBundleWatchServiceIfc {

	private static final String CONFIG_FILE = "etc/persistentwatch.txt";
	private BundleContext bc;

	@Activate
	public void doActivate(ComponentContext ctx) {
		this.bc = ctx.getBundleContext();
		doActivate();
	}

	public void setContext(BundleContext context) {
		System.out.println("Pling xx");
	}
	
	public void writeConfig(List<String> list) throws IOException {
		MFile.writeLines(new File(CONFIG_FILE), list, false);
	}

	public List<String> readConfig() throws IOException {
		File f = new File(CONFIG_FILE);
		if (!f.exists()) return new LinkedList<String>();
		return MFile.readLines(f,true);
	}

	@Override
	public void doActivate() {
		  ServiceReference<CommandProcessor> ref = bc.getServiceReference(CommandProcessor.class);
		  CommandProcessor commandProcessor= bc.getService(ref);
		  if (commandProcessor == null) {
			  System.out.println("Command processor not found");
			  return;
		  }
		  CommandSession commandSession=commandProcessor.createSession(System.in,System.out,System.err);
		  commandSession.put("APPLICATION",System.getProperty("karaf.name","root"));
		  commandSession.put("USER","karaf");
		  
		  try {
			  List<String> list = readConfig();
			  for (String bundle : list)
				  commandSession.execute("bundle:watch " + bundle);
		  } catch (Throwable t) {
			  t.printStackTrace();
		  }
	}

}
