package de.mhus.karaf.xdb.adb;

import java.util.Map;
import java.util.TreeSet;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.karaf.adb.AdbUtil;
import de.mhus.lib.karaf.adb.DbManagerService;

@Command(scope = "adb", name = "mapping", description = "Print the mapping table of a ADB DataSource")
@Service
public class CmdMapping implements Action {

	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;
		
	@Override
	public Object execute() throws Exception {
		
		DbManagerService service = AdbUtil.getService(serviceName);
		
		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Key","Mapping");
		
		Map<String, Object> map = service.getManager().getNameMapping();
		for (String entry : new TreeSet<String>(map.keySet())) {
			table.addRowValues(entry, String.valueOf(map.get(entry)) );
		}
		
		table.print(System.out);
		return null;
	}
	

}
