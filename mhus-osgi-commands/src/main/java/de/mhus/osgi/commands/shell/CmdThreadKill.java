package de.mhus.osgi.commands.shell;

import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

@Command(scope = "java", name = "kill", description = "Kill a thread with an exception")
public class CmdThreadKill implements Action {

	@Argument(index=0, name="thread", required=true, description="Thread Id", multiValued=false)
    String threadId;

	@Argument(index=1, name="message", required=true, description="Exception Message", multiValued=false)
    String message;
	
	@Argument(index=2, name="exception", required=false, description="Exception Name", multiValued=false)
    String exceptionName;
	
	@SuppressWarnings("deprecation")
	@Override
	public Object execute(CommandSession session) throws Exception {

		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		for (Thread thread : traces.keySet()) {
			if (String.valueOf(thread.getId()).equals(threadId) || thread.getName().equals(threadId)) {
				Throwable exception = new Throwable(message);
				if (exceptionName != null) {
					exception = (Throwable) Class.forName(exceptionName).getConstructor(String.class).newInstance(message);
				}
				thread.stop(exception);
				return null;
			}
		}
		
		return null;
	}

}
