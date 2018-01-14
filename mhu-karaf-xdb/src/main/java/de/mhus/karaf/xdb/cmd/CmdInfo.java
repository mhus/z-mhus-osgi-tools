package de.mhus.karaf.xdb.cmd;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.karaf.xdb.adb.AdbXdbApi;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.model.Field;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "xdb", name = "info", description = "Show information of a type")
@Service
public class CmdInfo implements Action {
	
	@Argument(index=0, name="type", required=true, description="Type to select", multiValued=false)
    String typeName;

	@Option(name="-a", description="Api Name",required=false)
	String apiName = CmdUse.api;

	@Option(name="-s", description="Service Name",required=false)
	String serviceName = CmdUse.service;

	@Override
	public Object execute() throws Exception {
		
		XdbApi api = XdbUtil.getApi(apiName);
		XdbType<?> type = api.getType(serviceName, typeName);
		
		
		List<String> fieldNames = type.getAttributeNames();
		fieldNames.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				boolean pk1 = type.isPrimaryKey(o1);
				boolean pk2 = type.isPrimaryKey(o2);
				if (pk1 == pk2)
					return o1.compareTo(o2);
				if (pk1) return -1;
				//if (pk2) return 1;
				return 1;
			}
		});

		
		ConsoleTable out = new ConsoleTable();
		out.setHeaderValues("Field Name","Type","PrimaryKey","Persistent","Mapping");
		for (String name : fieldNames) {
			out.addRowValues(name, type.getAttributeType(name), type.isPrimaryKey(name), type.isPersistent(name), type.getTechnicalName(name) );
		}
/*
		DbManagerService service = AdbUtil.getService(serviceName);
		Class<?> type = AdbUtil.getType(service, typeName);
		
		String regName = service.getManager().getRegistryName(type);
		Table tableInfo = service.getManager().getTable(regName);
		
		ConsoleTable out = new ConsoleTable();
		out.setHeaderValues("Field Name","Type","PrimaryKey","Persistent","Mapping");
		
		LinkedList<String> primaryNames = new LinkedList<>();
		for (Field f : tableInfo.getPrimaryKeys())
			primaryNames.add(f.getName());
		
		for (Field f : tableInfo.getFields())
			out.addRowValues(
					f.getName(), 
					f.getType().getSimpleName(), 
					String.valueOf(primaryNames.contains(f.getName())), 
					String.valueOf(f.isPersistent()),
					f.getMappedName() 
					);
*/
		out.print(System.out);
		
		return null;
	}
	

}
