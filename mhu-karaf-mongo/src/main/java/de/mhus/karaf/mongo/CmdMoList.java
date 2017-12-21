package de.mhus.karaf.mongo;

import java.util.List;
import java.util.TreeSet;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.adb.Persistable;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.mongo.MoManager;

@Command(scope = "mo", name = "list", description = "List Mongo Managers")
@Service
public class CmdMoList implements Action {

	@Override
	public Object execute() throws Exception {
		
		ConsoleTable out = new ConsoleTable();

		out.setHeaderValues("Nr","Service", "Schema", "DataSource", "Managed Types");
		int cnt = 0;
		for (MoManagerService service : new TreeSet<MoManagerService>( MongoUtil.getManagerServices() ) )  {
			MoManager manager = service.getManager();
			if (manager == null)
				out.addRowValues("*"+cnt,service.getServiceName(), "[disconnected]",service.getMongoDataSourceName(),"" );
			else {
				List<Class<? extends Persistable>> types = manager.getManagedTypes();
				if (types.size() == 0)
					out.addRowValues("*"+cnt,service.getServiceName(), manager.getSchema().getClass().getSimpleName(),service.getMongoDataSourceName(), "" );
				else {
					boolean first = true;
					for (Class<? extends Persistable> type : types) {
						if (first) {
							out.addRowValues("*"+cnt,service.getServiceName(), manager.getSchema().getClass().getSimpleName(),service.getMongoDataSourceName(), type.getSimpleName() );
						} else {
							out.addRowValues("","", "","", type.getSimpleName() );
						}
						first = false;
					}
				}
					
			}
		}
		
		out.print(System.out);
		return null;
	}

}
