package de.mhus.karaf.xdb.cmd;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.karaf.xdb.adb.AdbXdbApi;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbService;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.model.Field;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.core.MString;

@Command(scope = "xdb", name = "clone", description = "Load a object out of the database and store it as a clone.")
@Service
public class CmdClone implements Action {
	
	@Argument(index=0, name="type", required=true, description="Type to select", multiValued=false)
    String typeName;
	
	@Argument(index=1, name="search", required=true, description="Id of the object to clone or query", multiValued=false)
    String search;

	@Argument(index=2, name="attributes", required=false, description="Attributes for the initial creation", multiValued=true)
    String[] attributes;
	
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

		Object output = null;

		XdbApi api = XdbUtil.getApi(apiName);
		
		XdbType<?> type = api.getType(serviceName, typeName);
		
		for (Object object : XdbUtil.createObjectList(type, search)) {
			
			System.out.println(">>> CLONE " + object);
			HashMap<String, Object> attrObj = null;
			attrObj = new HashMap<>();
			if (attributes != null) {
				for (String item : attributes) {
					String key = MString.beforeIndex(item, '=').trim();
					String value = MString.afterIndex(item, '=').trim();
					attrObj.put(key, value);
				}
			}
			
			for (Entry<String, Object> entry : attrObj.entrySet()) {
				String name = entry.getKey();
				Object v = XdbUtil.prepareValue(type, name, entry.getValue());
				System.out.println("--- SET " + name + "  = " + v );
				XdbUtil.setValue(type,object,name,v);
			}
			
//			for (String name : type.getAttributeNames()) {
//				if (attrObj.containsKey(name)) {
//					Object v = type.prepareValue(name, attrObj.get(name) );
//					System.out.println("--- SET " + name + "  = " + v );
//					XdbUtil.setValue(type, object, name, v);
//				}
//			}
			
			System.out.print("*** CREATE ");
			type.createObject(object);
			System.out.println(type.getIdAsString(object));

			output = object;
		}
		
		if (outputParam != null)
			session.put(outputParam, output);
		return null;

/*		
		DbManagerService service = AdbUtil.getService(serviceName);
		Class<?> type = AdbUtil.getType(service, typeName);
		String regName = service.getManager().getRegistryName(type);
		Table tableInfo = service.getManager().getTable(regName);

		for (Object object : AdbUtil.getObjects(service, type, id)) {
		
			System.out.println(">>> CLONE " + object);
			HashMap<String, Object> attrObj = null;
			attrObj = new HashMap<>();
			if (attributes != null) {
				for (String item : attributes) {
					String key = MString.beforeIndex(item, '=').trim();
					String value = MString.afterIndex(item, '=').trim();
					attrObj.put(key, value);
				}
			}
			
			for (Field f : tableInfo.getFields()) {
				if (attrObj.containsKey(f.getName())) {
					Object v = AdbUtil.createAttribute(f.getType(), attrObj.get(f.getName()) );
					System.out.println("--- SET " + f.getName() + "  = " + v );
					f.set(object, v);
				}
			}
			
			System.out.print("*** CREATE");
			service.getManager().createObject(regName, object);
			for (Field f : tableInfo.getPrimaryKeys()) {
				System.out.print(" ");
				System.out.print(f.get(object));
			}
			output = object;
			System.out.println();
		}		
		if (outputParam != null)
			session.put(outputParam, output);
		return null;
*/
	}
	

}
