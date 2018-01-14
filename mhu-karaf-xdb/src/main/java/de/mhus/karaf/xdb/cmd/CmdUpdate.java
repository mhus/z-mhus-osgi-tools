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
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.model.Field;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.core.MString;

@Command(scope = "xdb", name = "update", description = "Update a single object in database")
@Service
public class CmdUpdate implements Action {
	
	@Argument(index=0, name="type", required=true, description="Type to select", multiValued=false)
    String typeName;
	
	@Argument(index=1, name="search", required=true, description="Id of the object or query", multiValued=false)
    String search;

	@Argument(index=2, name="attributes", required=false, description="Attributes to update, e.g user=alfons", multiValued=true)
    String[] attributes;
	
	@Option(name="-x", description="Output parameter",required=false)
	String outputParam = null;

	@Option(name="-f", description="Force Save",required=false)
	boolean force = false;
	
	@Option(name="-w", description="RAW Save",required=false)
	boolean raw = false;
	
	@Option(name="-a", description="Api Name",required=false)
	String apiName = CmdUse.api;

	@Option(name="-s", description="Service Name",required=false)
	String serviceName = CmdUse.service;

    @Reference
    private Session session;

	@Override
	public Object execute() throws Exception {
		
		HashMap<String, Object> attrObj = null;
		attrObj = new HashMap<>();
		if (attributes != null) {
			for (String item : attributes) {
				String key = MString.beforeIndex(item, '=').trim();
				String value = MString.afterIndex(item, '=').trim();
				attrObj.put(key, value);
			}
		}

		Object output = null;
		
		XdbApi api = XdbUtil.getApi(apiName);
		XdbType<?> type = api.getType(serviceName, typeName);
		
		for (Object object : XdbUtil.createObjectList(type, search)) {
			System.out.println(">>> UPDATE " + object);
			
			for (Entry<String, Object> entry : attrObj.entrySet()) {
				String name = entry.getKey();
				Object v = XdbUtil.prepareValue(type, name, entry.getValue());
				System.out.println("--- SET " + name + "  = " + v );
				XdbUtil.setValue(type,object,name,v);
			}
			
//			for (String name : type.getAttributeNames()) {
//				if (attrObj.containsKey(name)) {
//					Object v = type.prepareValue(name, attrObj.get(name));
//					System.out.println("--- SET " + name + "  = " + v );
//					XdbUtil.setValue(type, object, name, v);
//				}
//			}
			
			System.out.println("*** SAVE");
			if (force)
				type.saveObjectForce(object, raw);
			else
				type.saveObject(object);
			output = object;
			
		}

/*		
		DbManagerService service = AdbUtil.getService(serviceName);
		Class<?> type = AdbUtil.getType(service, typeName);
		
		HashMap<String, Object> attrObj = null;
		attrObj = new HashMap<>();
		if (attributes != null) {
			for (String item : attributes) {
				String key = MString.beforeIndex(item, '=').trim();
				String value = MString.afterIndex(item, '=').trim();
				attrObj.put(key, value);
			}
		}
		
		String regName = service.getManager().getRegistryName(type);
		Table tableInfo = service.getManager().getTable(regName);
		
		for (Object object : AdbUtil.getObjects(service, type, id)) {
		
			System.out.println(">>> UPDATE " + object);
			
			for (Field f : tableInfo.getFields()) {
				if (attrObj.containsKey(f.getName())) {
					Object v = AdbUtil.createAttribute(f.getType(), attrObj.get(f.getName()) );
					System.out.println("--- SET " + f.getName() + "  = " + v );
					f.set(object, v );
				}
			}
			
			System.out.println("*** SAVE");
			if (force)
				service.getManager().saveObjectForce(regName, object, raw);
			else
				service.getManager().saveObject(regName, object);
			output = object;
		}	
		*/
		if (outputParam != null)
			session.put(outputParam, output);
		return null;
	}
	

}
