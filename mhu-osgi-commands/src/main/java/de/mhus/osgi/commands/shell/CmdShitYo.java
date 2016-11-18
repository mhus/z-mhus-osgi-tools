package de.mhus.osgi.commands.shell;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "mhus", name = "shityo", description = "Command to do some shit")
@Service
public class CmdShitYo implements Action {

	@Argument(index=0, name="cmd", required=true, description="memkill,stackkill", multiValued=false)
    String cmd;

	@Argument(index=1, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;
    
	@Override
	public Object execute() throws Exception {

		if (cmd.equals("memkill")) {
			String kill = "killkill";
			while (true) {
				kill = kill + kill;
				System.out.println(kill.length() + " " + Runtime.getRuntime().freeMemory() );
			}
		} else
		if (cmd.equals("stackkill")) {
			doInfinity();
		}
		
		return null;
	}

	private void doInfinity() {
		doInfinity();
	}

}
