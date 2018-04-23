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
package de.mhus.osgi.vaadinkarafbridge.impl;

import java.io.PrintStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceList", description = "List all resource providers")
@Service
public class CmdVaadinResourceList implements Action {

	@Option(name="-f", aliases="--full", description="Full output",required=false)
	boolean full = false;

	@Reference
	private VaadinConfigurableResourceProviderAdmin provider;

	@Override
	public Object execute() throws Exception {
		PrintStream out = System.out;
		//session.getConsole();
		ConsoleTable table = new ConsoleTable(full);
		table.setHeaderValues("Bundle","Resources");
		for (String s : provider.getResourceBundles()) {
			
			StringBuilder res = new StringBuilder();
			boolean first = true;
			for (String p : provider.getResourcePathes(s)) {
				if (!first) res.append(',');
				res.append(p);
				first = false;
			}
			table.addRowValues(s,res.toString());
		}
		table.print(out);
		out.flush();
		return null;
	}

}
