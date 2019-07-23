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
package de.mhus.karaf.commands.mhus;

import java.util.Collection;
import java.util.Comparator;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.config.PropertiesConfig;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.sql.analytics.SqlAnalytics;
import de.mhus.lib.sql.analytics.SqlAnalyzer;
import de.mhus.lib.sql.analytics.SqlReporter;
import de.mhus.lib.sql.analytics.SqlRuntimeAnalyzer;
import de.mhus.lib.sql.analytics.SqlRuntimeAnalyzer.Container;
import de.mhus.lib.sql.analytics.SqlRuntimeWarning;
import de.mhus.lib.sql.analytics.SqlRuntimeWriter;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

@Command(scope = "mhus", name = "sql", description = "Sql tooling")
@Service
public class CmdSql extends AbstractCmd {

    @Reference
    private Session session;

	@Argument(index=0, name="cmd", required=true, description="Command:\n"
			+ " set - sets analytics tool (analyzer,writer,warning,reporter,<class>),\n"
			+ " reset - remove analytics tool,\n"
			+ " list - print analysis data,\n"
			+ "", multiValued=false)
    String cmd;

	@Argument(index=1, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

	@Override
	public Object execute2() throws Exception {
		
		switch (cmd) {
		case "set": {
			SqlAnalyzer analyzer = null;
			if (parameters == null || parameters[0].equals("analyzer"))
				analyzer = new SqlRuntimeAnalyzer();
			else
			if (parameters[0].equals("writer"))
				analyzer = new SqlRuntimeWriter();
			else
			if (parameters[0].equals("warning"))
				analyzer = new SqlRuntimeWarning();
			else
			if (parameters[0].equals("reporter"))
				analyzer = new SqlReporter();
			else {
				OsgiBundleClassLoader loader = new OsgiBundleClassLoader();
				analyzer = (SqlAnalyzer) loader.loadClass(parameters[0]).getDeclaredConstructor().newInstance();
			}
			
			if (parameters != null)
				analyzer.doConfigure(new PropertiesConfig(MProperties.explodeToProperties(parameters)));
			
			SqlAnalytics.setAnalyzer(analyzer);
			System.out.println(analyzer);
		} break;
		case "reset": {
			SqlAnalytics.setAnalyzer(null);
			System.out.println("OK");
		} break;
		case "list": {
			SqlAnalyzer analyzer = SqlAnalytics.getAnalyzer();
			if (analyzer instanceof SqlRuntimeAnalyzer) {
				Collection<Container> data = ((SqlRuntimeAnalyzer)analyzer).getData();
				ConsoleTable table = new ConsoleTable(tableAll, tblOpt);
				table.setHeaderValues("Count","Runtime","R/C","Sql");
				for (Container d : data) {
					table.addRowValues(d.getCnt(),d.getRuntime(), d.getRuntime() / (long)d.getCnt(), d.getSql());
				}
				table.sort(2, new Comparator<String>() {
					
					@Override
					public int compare(String o1, String o2) {
						return -Integer.compare(M.c(o1, 0),M.c(o2, 0));
					}
				});
				table.print(System.out);
			} else {
				System.out.println(analyzer);
			}
		} break;
		default:
			System.out.println("Unknown cmd");
		}
		
		
		return null;
	}

}
