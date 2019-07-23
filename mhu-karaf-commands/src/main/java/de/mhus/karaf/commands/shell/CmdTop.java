/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.shell;

import java.lang.Thread.State;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.MSystem.TopThreadInfo;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "java", name = "top", description = "Print thread status information")
@Service
public class CmdTop extends AbstractCmd {

    @Option(name = "-s", aliases = { "--stacktrace" }, description = "print also stack traces extract", required = false, multiValued = false)
    boolean stackAlso;
	
    @Option(name = "--orderid", description = "order by id", required = false, multiValued = false)
    boolean orderId;
    
    @Option(name = "-n", aliases = { "--ordername" }, description = "order by name", required = false, multiValued = false)
    boolean orderName;
    
    @Option(name = "-c", aliases = { "--ordercputime" }, description = "order by cputime", required = false, multiValued = false)
    boolean orderCpuTime = true;
    
    @Option(name = "-r", aliases = { "--running" }, description = "Running only", required = false, multiValued = false)
    boolean running;
    
    @Option(name = "-i", aliases = { "--interval" }, description = "Interval", required = false, multiValued = false)
    long sleep = 2000;
    
    @Option(name = "-o", aliases = { "--once" }, description = "Only once", required = false, multiValued = false)
    boolean once = false;
    
    @Option(name = "-t", aliases = { "--time" }, description = "order by time", required = false, multiValued = false)
    boolean orderTime = false;
    
    @Option(name = "-a", aliases = { "--absolut" }, description = "print absolut values", required = false, multiValued = false)
    boolean absolut = false;

    @Option(name = "-x", aliases = { "--raw" }, description = "Simple output format, set lines to output", required = false, multiValued = false)
    int raw = 0;

    DecimalFormat twoDForm = new DecimalFormat("#.00");
    
	@Override
	public Object execute2() throws Exception {

		Console console = Console.create();
		
		while (true) {
			List<TopThreadInfo> threads = MSystem.threadTop(sleep);
			if (running)
				threads.removeIf(i -> { return i.getThread().getState() != State.RUNNABLE; });
			
			if (orderId) {
				Collections.sort(threads, new Comparator<TopThreadInfo>() {

					@Override
					public int compare(TopThreadInfo o1, TopThreadInfo o2) {
						return Long.compare(o1.getThread().getId(), o2.getThread().getId());
					}
					
				});
			} else
			if (orderName) {
				Collections.sort(threads, new Comparator<TopThreadInfo>() {

					@Override
					public int compare(TopThreadInfo o1, TopThreadInfo o2) {
						return o1.getThread().getName().compareTo(o2.getThread().getName());
					}
				});
			} else
			if (orderTime) {
				Collections.sort(threads, new Comparator<TopThreadInfo>() {

					@Override
					public int compare(TopThreadInfo o1, TopThreadInfo o2) {
						return Long.compare(o2.getCpuTotal(), o1.getCpuTotal());
					}
					
				});
			} else
			if (orderCpuTime) {
				Collections.sort(threads, new Comparator<TopThreadInfo>() {

					@Override
					public int compare(TopThreadInfo o1, TopThreadInfo o2) {
						return Long.compare(o2.getCpuTime(), o1.getCpuTime());
					}
					
				});
			}

			if (raw > 0) {
				System.out.println("Id;Name;Status;Cpu;User;Time;Stacktrace");
				int cnt = 0;
				for (TopThreadInfo t : threads) {
					if (absolut) {
						System.out.println(
							t.getThread().getId() + ";"+ 
							t.getThread().getName() + ";"+ 
							t.getThread().getState() + ";"+ 
							t.getCpuTime() + ";"+ 
							t.getUserTime() + ";"+ 
							t.getCpuTotal() + ";" +
							(stackAlso ? toString( t.getStacktrace() ) : "") 
						);
					} else {
						System.out.println(
							t.getThread().getId() + ";"+ 
							t.getThread().getName() + ";"+ 
							t.getThread().getState() + ";"+ 
							twoDForm.format(t.getCpuPercentage()) + ";"+
							twoDForm.format(t.getUserPercentage()) + ";"+ 
							MPeriod.getIntervalAsStringSec(t.getCpuTotal() / 1000000 ) + ";"+
							(stackAlso ? toString( t.getStacktrace() ) : "") 
						);
					}
					cnt++;
					if (cnt >= raw) break;
				}
				System.out.println();
			} else {
				ConsoleTable table = new ConsoleTable(tableAll,tblOpt);
				int height = console.getHeight();
				int width = console.getWidth();
				table.setHeaderValues("Id", "Name", "Status", "Cpu", "User", "Time", "Stacktrace");
				table.getHeader().get(1).weight = 1;
				if (stackAlso)
					table.getHeader().get(6).weight = 1;
				table.setMaxTableWidth(width);
	//			table.setMaxColSize(Math.max( (width - 60) / 2, 30) );
				for (TopThreadInfo t : threads) {
					if (table.size() + 3 >= height) break;
					if (absolut) {
						table.addRowValues(
								t.getThread().getId(), 
								t.getThread().getName(), 
								t.getThread().getState(), 
								t.getCpuTime(), 
								t.getUserTime(), 
								t.getCpuTotal(),
								stackAlso ? toString( t.getStacktrace() ) : "" );
					} else {
						table.addRowValues(
								t.getThread().getId(), 
								t.getThread().getName(), 
								t.getThread().getState(), 
								twoDForm.format(t.getCpuPercentage()), 
								twoDForm.format(t.getUserPercentage()), 
								MPeriod.getIntervalAsStringSec(t.getCpuTotal() / 1000000 ),
								stackAlso ? toString( t.getStacktrace() ) : "" );
					}
				}
	
				console.cleanup();
				console.setCursor(0, 0);
				table.print(System.out);
			}
			
			if (once) break;
		}

		return null;
	}

	public static String toString(StackTraceElement[] trace) {
		StringBuilder sb = new StringBuilder();
		if (trace == null)
			return sb.toString();

		for (int i = 0; i < trace.length; i++) {
			String cName = trace[i].getClassName();
			if (
				!cName.startsWith("sun.")
				&&
				trace[i].getLineNumber() >= 0
				&&
				!cName.startsWith("java.util.")
				&&
				!cName.startsWith("java.net.")
				&&
				!cName.startsWith("com.google.common.")
				&&
				!cName.startsWith("org.apache.common.")
				&&
				!cName.startsWith("java.lang.")
			) {
				if (sb.length() > 0) sb.append("/");
				sb.append(cName).append('.')
						.append(trace[i].getMethodName());
			}
		}
		String str = sb.toString();
		return str;
	}

}
