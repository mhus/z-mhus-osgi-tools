package de.mhus.osgi.commands.shell;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.util.ArrayIterator;

@Command(scope = "shell", name = "run", description = "Run Gogo Script")
public class CmdRun extends MLog implements Action {

	@Argument(index = 0, name = "command", description = "File or command", required = true, multiValued = false)
    private String fileName;

    @Option(name = "-d", aliases = { "--debug" }, description = "Print debug information", required = false, multiValued = false)
    boolean debug;

    @Option(name = "-c", aliases = { "--command" }, description = "direct command input", required = false, multiValued = false)
    boolean command;

    @Option(name = "-s", aliases = { "--sensitive" }, description = "do not separate commands by semikolon", required = false, multiValued = false)
    boolean sensitive;
    
	@Override
	public Object execute(CommandSession session) throws Exception {

		List<String> lines = null;
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
				if (line.startsWith("label:")) {
					lines.set(i, "#" + line);
					labels.put(line.substring(7), i);
				}
			}
			
		}
		
		public void execute(CommandSession session) throws Exception {
			pos = 0;
			while(true) {
				if (pos >= lines.size()) return;
				String line = lines.get(pos).trim();
				if (debug) System.out.println("--- " + pos + " " + line);
				pos++;
				
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
				if (line.startsWith("for ")) {
					Loop loop = new Loop(session,line.substring(4), pos);
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
				} else {
					session.execute(line);
				}
					
				
			}
		}

		private boolean findEndIf(CommandSession session) throws Exception {
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
				if (line.equals("done")) ifCnt--;
				
				if (ifCnt < 0) return true;
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
	}
	
	public class Loop {

		private String condition;
		private String varName;
		private Iterator iterator;
		private CommandSession session;
		private int startPos;

		public Loop(CommandSession session, String condition, int pos) throws Exception {
			this.session = session;
			this.startPos = pos;
			condition = condition.trim();
			varName = MString.beforeIndex(condition, ' ');
			condition = MString.afterIndex(condition, ' ');
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
				iterator = new ArrayIterator<String>( MString.split(String.valueOf(res), "\n") );
			}
		}

		public int getStartPos() {
			return startPos;
		}

		public void next() {
			Object next = iterator.next();
			session.put(varName, next);
		}

		public boolean hasNext() {
			return iterator != null && iterator.hasNext();
		}
		
	}
	
}
