package de.mhus.karaf.mongo;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.karaf.MOsgi;

@Command(scope = "mongo", name = "datasources", description = "List Mongo Datasources")
@Service
public class CmdMongoDataSources implements Action {

	@Override
	public Object execute() throws Exception {
		
		ConsoleTable table = new ConsoleTable();
		
		table.setHeaderValues("Ref","Name","Connected","Host","Port");
		for (de.mhus.lib.karaf.MOsgi.Service<MongoDataSource> ref : MOsgi.getServiceRefs(MongoDataSource.class, null)) {
			
			MongoDataSource service = ref.getService();
			table.addRowValues(ref.getReference().getProperty("lookup.name"),service.getName(),service.isConnected(),service.getHost(),service.getPort());
		}
		
		table.print(System.out);
		return null;
	}
	
}
