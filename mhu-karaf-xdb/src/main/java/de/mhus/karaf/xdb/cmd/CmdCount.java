package de.mhus.karaf.xdb.cmd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.karaf.xdb.adb.AdbXdbApi;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.model.Field;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.core.MString;

@Command(scope = "xdb", name = "count", description = "Select data from DB DataSource ant print the count of found objects")
@Service
public class CmdCount implements Action {
	
	@Argument(index=0, name="type", required=true, description="Type to select", multiValued=false)
    String typeName;
	
	@Argument(index=1, name="search", required=false, description="Select qualification", multiValued=false)
    String search;

	@Option(name="-x", description="Output parameter",required=false)
	String outputParam = null;
	
	@Option(name="-a", description="Api Name",required=false)
	String apiName = CmdUse.api;

	@Option(name="-s", description="Service Name",required=false)
	String serviceName = CmdUse.service;

    @Reference
    private Session session;

	@Override
	public Object execute() throws Exception {
				
		XdbApi api = XdbUtil.getApi(apiName);
		
		XdbType type = api.getType(serviceName, typeName);

		long count = type.count(search, null);
/*		
		DbManagerService service = AdbUtil.getService(serviceName);
		Class<?> type = AdbUtil.getType(service, typeName);
		
		HashMap<String, Object> attrObj = null;
		if (attributes != null) {
			attrObj = new HashMap<>();
			for (String item : attributes) {
				String key = MString.beforeIndex(item, '=').trim();
				String value = MString.afterIndex(item, '=').trim();
				attrObj.put(key, value);
			}
		}
		
		
		String regName = service.getManager().getRegistryName(type);
		Table tableInfo = service.getManager().getTable(regName);

		List<Field> pkeys = tableInfo.getPrimaryKeys();
		final HashSet<String> pkNames = new HashSet<>();
		for (Field f : pkeys)
			pkNames.add(f.getName());
		long count = service.getManager().getCountByQualification(type, qualification, attrObj);
 */				
		
		System.out.println(count);
		
		if (outputParam != null)
			session.put(outputParam, count);
		return null;
	}
	

}
