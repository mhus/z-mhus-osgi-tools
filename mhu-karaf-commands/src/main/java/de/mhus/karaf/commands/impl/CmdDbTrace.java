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
package de.mhus.karaf.commands.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import de.mhus.osgi.api.util.DataSourceUtil;
import de.mhus.osgi.commands.db.TraceDataSource;
import de.mhus.osgi.commands.impl.TracedConnection;

@Command(scope = "jdbc", name = "dbtrace", description = "Modify DB Trace")
@Service
public class CmdDbTrace implements Action {

	@Argument(index=0, name="source", required=true, description="Source Datasource", multiValued=false)
    String source;

	@Argument(index=1, name="option", required=true, description="enable / disable", multiValued=false)
    String option;

    @Reference
	private BundleContext context;

	private DataSourceUtil util;

	@Override
	public Object execute() throws Exception {

		DataSource ds = util.getDataSource(source);
		Connection con = ds.getConnection();
		if (!(con instanceof TracedConnection)) {
			System.out.println("The source is not a trace datasource");
			return null;
		}

		TraceDataSource tds = (TraceDataSource) ((TracedConnection)con).getDataSource();
		
		if (option.equals("enable")) {
			tds.setTrace(true);
		} else
		if (option.equals("disable")) {
			tds.setTrace(false);
		} else
		if (option.equals("log")) {
			tds.setTraceFile("");
		} else
		if (option.startsWith("file:")) {
			tds.setTraceFile(option.substring(5));
		}
		System.out.println("Datasource " + source + " Trace: " + tds.isTrace() + " File: " + tds.getTraceFile() );
		
		return null;
	}

	public void setContext(BundleContext context) {
        this.context = context;
        this.util = new DataSourceUtil(context);
    }

}
