package de.mhus.osgi.commands.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "jdbc", name = "datasources", description = "Show old datasources services")
@Service
public class CmdDatasources implements Action {

	@Reference
	private BundleContext context;
	
	@Override
	public Object execute() throws Exception {
		ConsoleTable table = new ConsoleTable();
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
