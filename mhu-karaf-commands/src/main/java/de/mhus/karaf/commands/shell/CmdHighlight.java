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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.console.ANSIConsole;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.errors.UsageException;

@Command(scope = "shell", name = "highlight", description = "Highlight the found parts")
@Service
public class CmdHighlight implements Action {

	@Argument(index=0, name="regex", required=true, description="Regex to search or starting with # sets the format for following regex foundings\n"
			+ "#<color> WHITE,BLACK,RED,GREEN,BLUE,YELLOW,MAGENTA,CYAN - set foreground\n"
			+ "#F<color> WHITE,BLACK,RED,GREEN,BLUE,YELLOW,MAGENTA,CYAN - set foreground\n"
			+ "#B<color> WHITE,BLACK,RED,GREEN,BLUE,YELLOW,MAGENTA,CYAN - set background\n"
			+ "#A<mark> - start mark\n"
			+ "#O<mark> - stop mark\n"
			+ "#I<boolean> - switch case sensitive\n"
			+ "#R<reference> - specify the inserted reference", multiValued=true)
    String[] regex;

	@Override
	public Object execute() throws Exception {
		
		InputStreamReader isr = new InputStreamReader(System.in, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);
	    String line = null;
	    
	    while ((line = br.readLine()) != null) {
	    	
	    	Console.COLOR fg = Console.COLOR.RED;
	    	Console.COLOR bg = null;
	    	String markStart = null;
	    	String markStop = null;
	    	boolean caseSensitive = false;
	    	int reference = 0;
	    	
	    	for (String r : regex) {
	    		if (r.startsWith("#") && !r.startsWith("##")) {
	    			r = r.substring(1);
    				try {
    					fg = Console.COLOR.valueOf(r.toUpperCase());
    				} catch (IllegalArgumentException e) {
		    			if (r.startsWith("F") || r.startsWith("f")) 
		    				fg = Console.COLOR.valueOf(r.substring(1).toUpperCase());
		    			else
		    			if (r.startsWith("B") || r.startsWith("b"))
		    				bg = Console.COLOR.valueOf(r.substring(1).toUpperCase());
		    			else
		    			if (r.startsWith("A") || r.startsWith("a"))
		    				markStart = r.substring(1);
		    			else
		    			if (r.startsWith("O") || r.startsWith("o"))
		    				markStop = r.substring(1);
		    			else
		    			if (r.startsWith("I") || r.startsWith("i"))
		    				caseSensitive = MCast.toboolean(r.substring(1), true);
		    			else
		    			if (r.startsWith("R") || r.startsWith("r"))
		    				reference = MCast.toint(r.substring(1), 0);
		    			else {
		    				throw new UsageException("Unknown format definition",line);
		    			}
    				}
	    		} else {
	    			if (r.startsWith("##")) r = r.substring(1); // remove first #
	    			
	    			String replacement = 
	    					  (fg != null ? ANSIConsole.ansiForeground(fg) : "") 
	    					+ (bg != null ? ANSIConsole.ansiBackground(bg) : "")
	    					+ (markStart != null ? markStart : "")
    						+"$" + reference
    						+ (markStop != null ? markStop : "")
    						+ (fg != null || bg != null ? ANSIConsole.ansiCleanup() : "");
	    			
	    			String search = 
	    					(caseSensitive ? "" : "(?i)")
	    					+ r;
	    			line = line.replaceAll(search, replacement);
	    			
	    		}
	    	}
	    	System.out.println(line);
	    }
	    
		return null;
	}

	
	
}
