package de.mhus.osgi.commands.watch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.karaf.MOsgi;

@Component(immediate=true,name="de.mhus.osgi.commands.watch.PersistenceWatch",provide=PersistenceWatch.class)
public class PersistenceWatchImpl extends MLog implements PersistenceWatch {

	private Timer timer;

	private HashSet<String> done = new HashSet<>();
	
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				doTask();
			}
		}, 10000, 60000);
	}
	

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		timer.cancel();
	}

	protected void doTask() {
		try {
			synchronized (done) {
				for (String line : readFile()) {
					try {
						if (!done.contains(line)) {
							  CommandProcessor commandProcessor=MOsgi.getService(CommandProcessor.class);
							  CommandSession commandSession=commandProcessor.createSession(System.in,System.out,System.err);						
							  
							  commandSession.put("APPLICATION",System.getProperty("karaf.name","root"));
							  commandSession.put("USER","karaf");
							  
							  commandSession.execute("bundle:watch " + line);
							done.add(line);
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
	}
	
	private void writeFile(List<String> content) throws IOException {
		MFile.writeLines(getFile(), content, false);
	}
	
	private File getFile() {
		return new File("etc/" + PersistenceWatch.class.getCanonicalName() + ".txt");
	}


	@Override
	public void add(String line) throws IOException {
		synchronized (done) {
			done.remove(line);
			List<String> content = readFile();
			if (content.contains(line))
				content.remove(line);
			content.add(line);
			writeFile(content);
		}
	}


	@Override
	public void remove(String line) throws IOException {
		synchronized (done) {
			done.remove(line);
			List<String> content = readFile();
			content.remove(line);
			writeFile(content);
		}
	}


	@Override
	public String[] list() throws IOException {
		synchronized (done) {
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
	public void clearDone() {
		synchronized (done) {
			done.clear();
		}
	}

}
