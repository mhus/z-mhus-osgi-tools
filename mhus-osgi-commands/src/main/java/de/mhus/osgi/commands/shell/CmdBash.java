package de.mhus.osgi.commands.shell;

import java.util.LinkedList;
import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;

@Command(scope = "shell", name = "bash", description = "Execute bash line")
public class CmdBash extends MLog implements Action {

	@Argument(index = 0, name = "command", description = "Execution bash command with arguments", required = true, multiValued = true)
    private List<String> bashArgs;

	@Override
	public Object execute(CommandSession arg0) throws Exception {

		LinkedList<String> args = new LinkedList<>();
		args.add("bash");
		args.add("-c");
		args.add(MString.join(bashArgs.iterator(), " "));
		
		
		
		ProcessBuilder builder = new ProcessBuilder(args);
		 
        PumpStreamHandler handler = new PumpStreamHandler(System.in, System.out, System.err, "Command" + args.toString());

        log().d("Executing", builder.command());
        Process p = builder.start();

        handler.attach(p);
        handler.start();

        log().d("Waiting for process to exit...");
        
        int status = p.waitFor();

       log().d("Process exited w/status", status);

        handler.stop();

        return null;
    }

}
