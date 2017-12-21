package de.mhus.karaf.mongo;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.adb.Persistable;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.mongo.MoManager;
import de.mhus.lib.mongo.MoSchema;

@Command(scope = "mo", name = "info", description = "MO info")
@Service
public class CmdMoInfo implements Action {

	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;

	@Argument(index=1, name="type", required=false, description="Type to select", multiValued=false)
    String typeName;

	@Override
	public Object execute() throws Exception {
		
		ConsoleTable out = new ConsoleTable();
		
		MoManagerService service = MApi.lookup(MoManagerAdmin.class).getService(serviceName);
		
		MoManager manager = service.getManager();
		MoSchema schema = manager.getSchema();
		
		if (MString.isEmpty(typeName)) {
			
			
			out.setHeaderValues("Name","Value");
			
			out.addRowValues("ServiceName",service.getServiceName());
			out.addRowValues("DataSource",service.getMongoDataSourceName());
			out.addRowValues("DatabaseName",schema.getDatabaseName());
//			out.addRowValues("",service.getSchema().initMapper(mapper););
		} else {
			
			Class<?> type = manager.getManagedType(typeName);
			
			
		}
		
		out.print(System.out);
		
		return null;
	}

}
