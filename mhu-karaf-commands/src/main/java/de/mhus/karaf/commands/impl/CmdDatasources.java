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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.util.DataSourceUtil;

@Command(scope = "jdbc", name = "datasources", description = "Show old datasources services")
@Service
public class CmdDatasources implements Action {

	@Option(name="-f", aliases="--full", description="Full output",required=false)
	boolean full = false;

	@Reference
	private BundleContext context;
	
	@Override
	public Object execute() throws Exception {
		ConsoleTable table = new ConsoleTable(full);
		table.setHeaderValues("Name","Url","Status");
		
        DataSourceUtil util = new DataSourceUtil(context);
        
        for ( ServiceReference<DataSource> reference : util.getDataSources()) {
        	DataSource service = context.getService(reference);
        	Object name = reference.getProperty(DataSourceUtil.SERVICE_JNDI_NAME_KEY);
        	String url = "";
        	String status = "OK";
        	
        	try {
        		Connection con = service.getConnection();
        		url = con.getMetaData().getURL();
        		con.close();
        	} catch (Throwable t) {
        		status = t.getMessage();
        	}
        	table.addRowValues(name,url,status);
        }
		
		table.print(System.out);
		return null;
	}

}
