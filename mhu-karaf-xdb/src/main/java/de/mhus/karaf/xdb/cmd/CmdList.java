package de.mhus.karaf.xdb.cmd;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.karaf.xdb.adb.AdbXdbApi;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbService;
import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "xdb", name = "list", description = "List all DB Services")
@Service
public class CmdList implements Action {

	@Option(name="-a", description="Api Name",required=false)
	String apiName = CmdUse.api;

	@Override
	public Object execute() throws Exception {
		
		XdbApi api = XdbUtil.getApi(apiName);

		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Service","Schema","DataSource","Managed Types");
		for (String serviceName : api.getServiceNames()) {
			XdbService service = api.getService(serviceName);
			if (service.isConnected()) {
				
				int c = 0;
				for (String typeName : service.getTypeNames() ) {
					if (c == 0) {
						table.addRowValues(
								serviceName,
								service.getSchemaName(),
								service.getDataSourceName(),
								typeName
							);
					} else {
						table.addRowValues(
								"",
								"",
								"",
								typeName
							);
						
					}
					c++;
				}
				if (c == 0) {
					table.addRowValues(
							serviceName,
							service.getSchemaName(),
							service.getDataSourceName(),
							""
						);
				}
			} else {
				table.addRowValues(
						serviceName,
						"[not connected]", 
						service.getDataSourceName(),
						""
					);
			}
		}
/*		
		DbManagerAdmin admin = AdbUtil.getAdmin();
		if (admin == null) {
			System.out.println("Admin not found");
			return null;
		}
		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Nr","Service","Schema","DataSource","Managed Types");
		// iterate all services
		
		int cnt = 0;

		for ( DbManagerService service : AdbUtil.getServices(false)) {
			if (service.isConnected()) {
				DbManager manager = service.getManager();
				
				int c = 0;
				for (Class<? extends Persistable> type : manager.getSchema().getObjectTypes()) {
					if (c == 0) {
						table.addRowValues(
								"*" + cnt,
								service.getServiceName(),
								manager.getSchema().getClass().getSimpleName(),
								service.getDataSourceName(),
								type.getSimpleName()
							);
					} else {
						table.addRowValues(
								"",
								"",
								"",
								"",
								type.getSimpleName()
							);
						
					}
					c++;
				}
				if (c == 0) {
					table.addRowValues(
							"*" + cnt,
							service.getServiceName(),
							manager.getSchema().getClass().getSimpleName(),
							service.getDataSourceName(),
							""
						);
				}
			} else {
				table.addRowValues(
						"*" + cnt,
						service.getServiceName(),
						"[not connected]", 
						service.getDataSourceName(),
						""
					);
			}
			cnt++;
		}
		*/
		table.print(System.out);
		return null;
	}

}
