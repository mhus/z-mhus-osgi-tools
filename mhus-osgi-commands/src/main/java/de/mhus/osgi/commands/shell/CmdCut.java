package de.mhus.osgi.commands.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;

@Command(scope = "shell", name = "cut", description = "Cut out selected parts of a line")
public class CmdCut implements Action {

    @Option(name = "-d", aliases = { "--delimiter" }, description = "Seperate parts by this regex", required = false, multiValued = false)
    String delim;
    @Option(name = "-e", aliases = { "--regex" }, description = "Regular expression", required = false, multiValued = false)
    String regex;
    @Option(name = "-r", aliases = { "--replace" }, description = "Replace regex", required = false, multiValued = false)
    String replace;
    @Option(name = "-f", aliases = { "--fields" }, description = "Fields", required = false, multiValued = false)
    String fields;
    @Option(name = "-p", aliases = { "--positions" }, description = "Positions", required = false, multiValued = false)
    String positions;
    @Option(name = "-j", aliases = { "--join" }, description = "Glue between fields", required = false, multiValued = false)
    String join = "";
    @Option(name = "-t", aliases = { "--trim" }, description = "Trim every single part", required = false, multiValued = false)
    boolean trim = false;
    
	private StringBuilder out;

	@Override
	public Object execute(CommandSession session) throws Exception {

		out = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(System.in, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;
	    while ((line = br.readLine()) != null) {

	    	if (MString.isSet(regex) && MString.isSet(replace))
	    		line = line.replaceAll(regex, replace);
	    	
	    	if (MString.isSet(delim))
	    		processDelim(line);
	    	else
	    	if (MString.isSet(positions))
	    		processPos(line);
	    	else
	    		out.append(line).append("\n");
	    		
	    }
		return out.toString();
	}

	private void processPos(String line) {
		boolean first = true;
		for (String p : fields.split(",")) {
			
			int start = 0;
			int stop = line.length()-1;
			
			if (p.indexOf('-') >= 0) {
				String startStr = MString.beforeIndex(p, '-');
				String stopStr = MString.afterIndex(p, '-');
				start = MCast.toint(startStr, start);
				stop = MCast.toint(stopStr, stop);
			} else {
				start = MCast.toint(p, -1);
				stop = start;
				if (start == -1) {
					if (first)
						first = false;
					else
						out.append(join);
					if (trim) p = p.trim();
					out.append(p);
					continue;
				}
			}
			
			if (first)
				first = false;
			else
				out.append(join);
			
			start = Math.max(0, start);
			stop = Math.min(line.length()-1, stop);
			if (start >= stop) continue;
			
			String x = line.substring(start, stop);
			if (trim) x = x.trim();
			out.append( x );
			
		}
		if (!first) out.append("\n");
	}

	private void processDelim(String line) {
		boolean first = true;
		String[] parts = line.split(delim);
		for (String p : fields.split(",")) {
			
			int start = 0;
			int stop = parts.length-1;
			
			if (p.indexOf('-') >= 0) {
				String startStr = MString.beforeIndex(p, '-');
				String stopStr = MString.afterIndex(p, '-');
				start = MCast.toint(startStr, start);
				stop = MCast.toint(stopStr, stop);
			} else {
				start = MCast.toint(p, -1);
				stop = start;
				if (start == -1) {
					if (first)
						first = false;
					else
						out.append(join);
					if (trim) p = p.trim();
					out.append(p);
					continue;
				}
			}
			
			for (int i = start; i <= stop; i++) {
				if (i >= 0 && i < parts.length) {
					if (first)
						first = false;
					else
						out.append(join);
					String x = parts[i];
					if (trim) x = x.trim();
					out.append(x);
				}
			}	
			
		}
		if (!first) out.append("\n");

	}

}
