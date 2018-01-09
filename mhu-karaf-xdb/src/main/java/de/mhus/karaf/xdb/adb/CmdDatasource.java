package de.mhus.karaf.xdb.adb;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.karaf.adb.AdbUtil;
import de.mhus.lib.karaf.adb.DbManagerService;

@Command(scope = "adb", name = "datasource", description = "Update ADB DataSource")
@Service
public class CmdDatasource implements Action {
	
	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;

	@Argument(index=1, name="source", required=false, description="Data Source", multiValued=false)
    String sourceName;
	
	@Override
	public Object execute() throws Exception {

		int cnt = 0;
		
		for ( DbManagerService service : AdbUtil.getAdmin().getServices()) {
//			if (service.isConnected()) {
				if (service.getClass().getCanonicalName().equals(serviceName)) {
					if (sourceName == null)
						service.updateManager(false);
					else
						service.setDataSourceName(sourceName);
					cnt++;
				}
//			}
		}
		System.out.println("Updated: " + cnt);
		return null;
	}

}
