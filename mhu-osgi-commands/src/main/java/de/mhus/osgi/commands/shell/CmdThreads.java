package de.mhus.osgi.commands.shell;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "java", name = "thread", description = "Print thread information")
public class CmdThreads implements Action {

	@Argument(index=0, name="thread", required=false, description="Thread Id", multiValued=false)
    String threadId;

    @Option(name = "-s", aliases = { "--stacktrace" }, description = "print also stack traces", required = false, multiValued = false)
    boolean stackAlso;
	
    @Option(name = "-i", aliases = { "--orderid" }, description = "order by id", required = false, multiValued = false)
    boolean orderId;
    
    @Option(name = "-n", aliases = { "--ordername" }, description = "order by name", required = false, multiValued = false)
    boolean orderName;
    
    @Option(name = "-g", aliases = { "--ordergroup" }, description = "order by group", required = false, multiValued = false)
    boolean orderGroup;
    
	@Override
	public Object execute(CommandSession session) throws Exception {

		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		
		List<Thread> threadList = new LinkedList<>(traces.keySet());
		
		if (orderId) {
			Collections.sort(threadList, new Comparator<Thread>() {

				@Override
				public int compare(Thread o1, Thread o2) {
					return Long.compare(o1.getId(), o2.getId());
				}
			});
		}
		
		if (orderName) {
			Collections.sort(threadList, new Comparator<Thread>() {

				@Override
				public int compare(Thread o1, Thread o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
		if (orderGroup) {
			Collections.sort(threadList, new Comparator<Thread>() {

				@Override
				public int compare(Thread o1, Thread o2) {
					return o1.getThreadGroup().getName().compareTo(o2.getThreadGroup().getName());
				}
			});
		}

		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Id", "Name", "Group", "Prio","Alive","Daemon");
		
		if (threadId == null) {
			
			for (Thread thread : threadList) {
				printThread(thread, table);
				if (stackAlso) {
					StackTraceElement[] stack = traces.get(thread);
					printStack(stack, table);
				}
			}
			
		} else {
			
			for (Thread thread : traces.keySet()) {
				if (String.valueOf(thread.getId()).equals(threadId) || thread.getName().equals(threadId)) {
					printThread(thread, table);
	
					StackTraceElement[] stack = traces.get(thread);
					printStack(stack,table);
				}
			}
			
		}
		table.print(System.out);
		return null;
	}

	private void printStack(StackTraceElement[] stack, ConsoleTable table) {
		for (StackTraceElement line : stack)
			table.addRowValues("","  at " + line,"","","","");
	}

	private void printThread(Thread thread, ConsoleTable table) {
		ThreadGroup g = thread.getThreadGroup();
		table.addRowValues(thread.getId(), thread.getName(), g == null ? "" : g.getName(), thread.getPriority(), thread.isAlive(), thread.isDaemon());
	}

}
