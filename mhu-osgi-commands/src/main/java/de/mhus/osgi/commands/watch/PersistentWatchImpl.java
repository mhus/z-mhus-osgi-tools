package de.mhus.osgi.commands.watch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import org.apache.karaf.bundle.core.BundleWatcher;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.karaf.MOsgi;

@Component(immediate=true,name="de.mhus.osgi.commands.watch.PersistentWatch",provide=PersistentWatch.class)
public class PersistentWatchImpl extends MLog implements PersistentWatch {

	private TimerIfc timer;
	private TimerTask timerTask;
//	private PersistentWatchConfig pwc;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		
//	    pwc = new PersistentWatchConfig();
//	    pwc.register(ctx.getBundleContext());
		
		timer = MOsgi.getTimer();
		timerTask = new TimerTask() {
			
			@Override
			public void run() {
				doTask();
			}
		};
		timer.schedule(timerTask, 10000, 60000);
	}
	

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		timerTask.cancel();
//		pwc.unregister();
	}

	protected void doTask() {
		try {
			synchronized (this) {
				BundleWatcher bundleWatcher = MOsgi.getService(BundleWatcher.class);
				List<String> watched = bundleWatcher.getWatchURLs();
				for (String line : readFile()) {
					try {
						if (!watched.contains(line)) {
							
							log().i("add",line);
							bundleWatcher.add(line);
							
							/*
							  CommandProcessor commandProcessor=MOsgi.getService(CommandProcessor.class);
							  CommandSession commandSession=commandProcessor.createSession(System.in,System.out,System.err);						
							  
							  commandSession.put("APPLICATION",System.getProperty("karaf.name","root"));
							  commandSession.put("USER","karaf");
							  
							  commandSession.execute("bundle:watch " + line);
							  */
						}
					} catch (Throwable t) {
						log().d(t);
					}
				}
			}
		} catch (Throwable t) {
			log().d(t);
		}
	}

	private List<String> readFile() throws IOException {
		try {
			return MFile.readLines(getFile(),true);
		} catch (FileNotFoundException e) {
			return new LinkedList<>();
		}
//		return pwc.readFile();
	}
	
	private void writeFile(List<String> content) throws IOException {
		MFile.writeLines(getFile(), content, false);
//		pwc.writeFile(content);
	}
	
	private File getFile() {
		return new File("etc/" + PersistentWatch.class.getCanonicalName() + ".cfg");
	}


	@Override
	public void add(String line) throws IOException {
		synchronized (this) {
			List<String> content = readFile();
			if (content.contains(line))
				content.remove(line);
			content.add(line);
			writeFile(content);
		}
	}


	@Override
	public void remove(String line) throws IOException {
		synchronized (this) {
			List<String> content = readFile();
			content.remove(line);
			writeFile(content);
		}
	}


	@Override
	public String[] list() throws IOException {
		synchronized (this) {
			return readFile().toArray(new String[0]);
		}
	}


	@Override
	public void watch() {
		doTask();
	}


	@Override
	public void clear() throws IOException {
		writeFile(new LinkedList<String>());
	}


	@Override
	public void remember() throws IOException {
		synchronized (this) {
			BundleWatcher bundleWatcher = MOsgi.getService(BundleWatcher.class);
			List<String> watched = bundleWatcher.getWatchURLs();
			writeFile(watched);
		}
	}


}
