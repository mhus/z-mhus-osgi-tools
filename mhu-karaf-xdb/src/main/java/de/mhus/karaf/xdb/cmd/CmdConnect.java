package de.mhus.karaf.xdb.cmd;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.karaf.xdb.adb.AdbXdbApi;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbService;

@Command(scope = "xdb", name = "connect", description = "Connect ADB DataSource")
@Service
public class CmdConnect implements Action {

	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;
	
	@Option(name="-u", aliases="--update", description="Causes the driver to reconnect to the datasource",required=false)
	boolean update = false;
	
	@Option(name="-c", aliases="--cleanup", description="Cleanup unised table field and indexes - this can delete additional data",required=false)
	boolean cleanup = false;

	@Option(name="-a", description="Api Name",required=false)
	String apiName = CmdUse.api;

	@Override
	public Object execute() throws Exception {

		XdbApi api = XdbUtil.getApi(apiName);

		XdbService service = api.getService(serviceName);
		
		if (update || cleanup)
			service.updateSchema(cleanup);
		else
			service.connect(); // this call will touch the service and connect to the database
		System.out.println("OK");
		
		return null;

/*		
		DbManagerService service = AdbUtil.getService(serviceName);
		if (service != null) {
			if (update || cleanup)
				service.updateManager(cleanup);
			else
				service.getManager(); // this call will touch the service and connect to the database
			System.out.println("OK");
		} else {
			System.out.println("Not found");
		}
		*/
	}

}
