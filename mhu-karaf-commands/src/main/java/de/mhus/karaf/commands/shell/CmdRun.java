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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.util.ArrayIterator;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "shell", name = "run", description = "Run Gogo Script")
@Service
public class CmdRun extends AbstractCmd {

	@Argument(index = 0, name = "command", description = "File or command", required = true, multiValued = true)
    private String[] cmd;

    @Option(name = "-d", aliases = { "--debug" }, description = "Print debug information", required = false, multiValued = false)
    boolean debug;

    @Option(name = "-i", aliases = { "--notinteruptable" }, description = "Not interuptable by pressing Ctrl+C, this will allow the script to process binary stdin", required = false, multiValued = false)
    boolean notInteruptable;

    @Option(name = "-c", aliases = { "--command" }, description = "direct command input", required = false, multiValued = false)
    boolean command;

    @Option(name = "-g", aliases = { "--goto" }, description = "First goto jump to start", required = false, multiValued = false)
    String go;
    
    @Option(name = "-s", aliases = { "--sensitive" }, description = "do not separate commands by semikolon", required = false, multiValued = false)
    boolean sensitive;
    
    @Reference
    private Session session;

	@Override
	public Object execute2() throws Exception {

		List<String> lines = null;
		String fileName = MString.join(cmd, ' ');
		if (command) {
			lines = new LinkedList<>();
			if (!sensitive)
				fileName = fileName.replace(";", "\n");
			for (String line : MString.split(fileName, "\n") )
				lines.add(line);
		} else {
			File file = new File(fileName);
			lines = MFile.readLines(file, false);
		}
		Context context = new Context(lines);
		context.execute(session);
		
        return null;
    }

	private class Context {

		private HashMap<String, Integer> labels = new HashMap<>();
		private List<String> lines;
		private int pos;
		private LinkedList<Loop> loops = new LinkedList<>();

		public Context(List<String> lines) {
			this.lines = lines;
			
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i).trim();
				if (line.startsWith("label ")) {
					String label = line.substring(6).trim();
					if (label.endsWith(":")) label = label.substring(0, label.length()-1).trim();
					labels.put(label, i);
				}
			}
			
		}
		
		public void execute(Session session) throws Exception {
			pos = 0;
			while(true) {
				// check stdin
				if (!notInteruptable) {
					while ( System.in.available() > 0 ) {
						int b = System.in.read();
						if (b < 0) {
							break;
						}
						System.out.print((char)b);
					}
				}
				
				String line = null;
				if (go != null) { // insert special goto command
					line = "goto " + go;
					go = null;
				} else {
					if (pos >= lines.size()) return;
					line = lines.get(pos).trim();
					if (debug) System.out.println("--- " + pos + " " + line);
					pos++;
				}
				
				if (line.startsWith("#") || line.length() == 0) {
					if (line.equals("#--")) {
						while (true) {
							if (pos >= lines.size()) {
								System.err.println("Start comment block without end");
								return;
							}
							line = lines.get(pos).trim();
							pos++;
							if (line.equals("--#")) continue;
						}
					}
					continue;
				}
				
				if (line.startsWith("goto ")) {
					String label = line.substring(5).trim();
					Integer newPos = labels.get(label);
					if (newPos == null) {
						int p = MCast.toint(label, -1);
						if (p >= 0)
							newPos = p;
					}
					if (newPos == null) {
						System.err.println("Label not found: " + label + " at " + (pos-1));
						return;
					}
					pos = newPos;
				} else
				if (line.startsWith("if ")) {
					if (isTrue(session.execute("%(" + line.substring(3) + ")" )) ) {
						
					} else {
						if (!findEndIf(session)) return;
					}
				} else
				if (line.startsWith("while ")) {
					Loop loop = new WhileLoop(session,line.substring(6), pos);
					if (loop.hasNext()) {
						loops.add(0, loop);
						loop.next();
					} else {
						if (!findDone()) return;
					}
				} else
				if (line.startsWith("for ")) {
					Loop loop = new ForLoop(session,line.substring(4), pos);
					if (loop.hasNext()) {
						loops.add(0, loop);
						loop.next();
					} else {
						if (!findDone()) return;
					}
					
				} else
				if (line.equals("else")) {
					if (!findEndIf(null)) return;
				} else
				if (line.equals("endif")) {
				} else
				if (line.startsWith("elseif ")) {
					if (!findEndIf(null)) return;
				} else
				if (line.equals("done")) {
					if (loops.size() < 1) {
						System.err.println("done without for");
						return;
					}
					Loop loop = loops.getFirst();
					if (loop.hasNext()) {
						loop.next();
						pos = loop.getStartPos();
					} else {
						loops.removeFirst();
					}
				} else
				if (line.equals("exit")) {
					return;
				} else
				if (line.startsWith("label ")) {
				} else {
					session.execute(line);
				}
					
				
			}
		}

		private boolean findEndIf(Session session) throws Exception {
			int l = pos-1;
			int ifCnt = 0;
			while (true) {
				if (pos >= lines.size()) {
					System.err.println("If with insufficien end at line " + l);
					return false;
				}
				String line = lines.get(pos).trim();
				pos++;
				
				if (line.startsWith("if ")) ifCnt++;
				else
				if (line.equals("else")) ifCnt--;
				else
				if (line.startsWith("elseif ")) {
					if (ifCnt == 0) {
						if (session == null) {
						} else
						if (isTrue(session.execute("%(" + line.substring(7) + ")" )) ) {
							return true;
						}
					}
				}
				else
				if (line.equals("endif")) ifCnt--;
				
				if (ifCnt < 0) return true;
			}
			
		}

		private boolean findDone() throws Exception {
			int l = pos-1;
			int ifCnt = 0;
			while (true) {
				if (pos >= lines.size()) {
					System.err.println("for with insufficien end at line " + l);
					return false;
				}
				String line = lines.get(pos).trim();
				pos++;
				
				if (line.startsWith("for ")) ifCnt++;
				else
				if (line.startsWith("while ")) ifCnt++;
				else
				if (line.equals("done")) ifCnt--;
				
				if (ifCnt < 0) return true;
			}
			
		}
	
	}
	
	private boolean isTrue(Object result) {
		if (result == null) {
			return false;
		}
		if (result instanceof String && ((String) result).equals("")) {
			return false;
		}
		if (result instanceof Number) {
			return ((Number) result).doubleValue() != 0.0d;
		}
		if (result instanceof Boolean) {
			return (Boolean) result;
		}
		return true;
	}	
	
	public abstract class Loop {
		
		protected int startPos;
		protected Session session;
		protected String condition;
		
		public Loop(Session session, String condition, int pos) throws Exception {
			this.session = session;
			this.condition = condition.trim();
			this.startPos = pos;
		}
		public int getStartPos() {
			return startPos;
		}

		public abstract void next();
		public abstract boolean hasNext() throws Exception;

	}
	
	public class WhileLoop extends Loop {
		public WhileLoop(Session session, String condition, int pos) throws Exception {
			super(session, condition, pos);
		}

		@Override
		public void next() {
		}

		@Override
		public boolean hasNext() throws Exception {
			Object res = session.execute( "%(" + condition + ")" );
			return isTrue(res);
		}		
	}
	public class ForLoop extends Loop {

		private String varName;
		private Iterator<?> iterator;

		@SuppressWarnings("rawtypes")
		public ForLoop(Session session, String condition, int pos) throws Exception {
			super(session, condition, pos);
			varName = MString.beforeIndex(condition, ' ');
			condition = MString.afterIndex(condition, ' ');
			
			int p = condition.indexOf("in ");
			if (p > -1 ) {
				//TODO read options like delimiter
				condition = condition.substring(p+3).trim();
			}
			this.condition = condition;
			Object res = session.execute(condition);
			if (res == null) {
				
			} else
			if (res instanceof Map) {
				iterator = ((Map)res).entrySet().iterator();
			} else
			if (res instanceof List) {
				iterator = ((List)res).iterator();
			} else
			if (res instanceof Set) {
				iterator = ((Set)res).iterator();
			} else {
				String[] array = MString.split(String.valueOf(res), "\n");
				int stop = array.length;
				if (array.length > 0 && MString.isEmpty(array[stop-1])) stop--;
				iterator = new ArrayIterator<String>( array, 0, stop );
				
			}
		}

		@Override
		public int getStartPos() {
			return startPos;
		}

		@Override
		public void next() {
			Object next = iterator.next();
			session.put(varName, next);
		}

		@Override
		public boolean hasNext() {
			return iterator != null && iterator.hasNext();
		}
		
	}
	
}
