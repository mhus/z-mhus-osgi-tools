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
package de.mhus.osgi.commands.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;

@Command(scope = "shell", name = "cut", description = "Cut out selected parts of a line")
@Service
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
    @Option(name = "-n", description = "Add not new line at the end", required = false, multiValued = false)
    boolean n = false;
    @Option(name = "-x", description = "Do not ignore empty lines", required = false, multiValued = false)
    boolean empty = false;
    
    @Option(name = "-m", aliases = { "--mode" }, description = "How to handle the value, text, array -f , list -p", required = false, multiValued = false)
    String mode;

	private StringBuilder out;

	@Override
	public Object execute() throws Exception {

		out = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(System.in, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;
	    while ((line = br.readLine()) != null) {
	    	out.setLength(0);
	    	
	    	if (mode == null || mode.equals("text")) {
		    	if (MString.isSet(regex) && MString.isSet(replace))
		    		line = line.replaceAll(regex, replace);
		    	
		    	if (MString.isSet(delim))
		    		processDelim(line);
		    	else
		    	if (MString.isSet(positions))
		    		processPos(line);
		    	else {
		    		out.append(line);
		    		if (!n) out.append("\n");
		    	}
		    	
	    	} else
	    	if (mode.equals("array")) {
	    		line = line.trim();
	    		if (line.startsWith("[") && line.endsWith("]")) {
	    			line = line.substring(1, line.length()-1);
	    			String[] parts = line.split(",");
	    			HashMap<String, String> map = new HashMap<>();
	    			for (int i = 0; i < parts.length; i++) {
	    				int p = parts[i].indexOf('=');
	    				if (p >= 0 ) {
	    					String v = parts[i].substring(p+1);
	    					if (trim) v = v.trim();
	    					map.put(parts[i].substring(0, p).trim(), v);
	    				}
	    			}
	    			if (fields != null) {
	    				for (String f : fields.split(",")) {
	    					if (out.length() > 0)
	    						out.append(join);
	    					if (map.containsKey(f))
	    						out.append(map.get(f));
	    				}
	    			}
		    		if (!n) out.append("\n");
	    		}
	    	} else
	    	if (mode.equals("list")) {
	    		line = line.trim();
	    		if (line.startsWith("[") && line.endsWith("]")) {
	    			line = line.substring(1, line.length()-1);
	    			String[] parts = line.split(",");
	    			for (int i = 1; i < parts.length; i++) parts[i] = parts[1].substring(1);
	    			if (positions != null) {
	    				for (String p : positions.split(",")) {
	    					int pos = MCast.toint(p, -1);
	    					if (pos >=0 && pos < parts.length) {
		    					if (out.length() > 0)
		    						out.append(join);
		    					out.append(parts[pos]);
	    					}
	    				}
	    			}
		    		if (!n) out.append("\n");
	    		}
	    	}
	    	
	    	if (empty || out.length() > 0)
	    		System.out.print(out);

	    }
//		return out.toString();
	    return null;
	}

	private void processPos(String line) {
		boolean first = true;
		for (String p : positions.split(",")) {
			
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
		if (!n && !first) out.append("\n");
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
		if (!n && !first) out.append("\n");

	}

}
