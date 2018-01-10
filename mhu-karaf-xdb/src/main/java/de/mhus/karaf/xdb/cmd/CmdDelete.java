package de.mhus.karaf.xdb.cmd;

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

@Command(scope = "xdb", name = "delete", description = "Delete a single object from database")
@Service
public class CmdDelete implements Action {

	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;

	@Argument(index=1, name="type", required=true, description="Type to select", multiValued=false)
    String typeName;
	
	@Argument(index=2, name="search", required=true, description="Id of the object or query", multiValued=false)
    String search;
	
	@Option(name="-x", description="Output parameter",required=false)
	String outputParam = null;

	@Option(name="-a", description="Api Name",required=false)
	String apiName = CmdXdbApi.api;

    @Reference
    private Session session;

	@Override
	public Object execute() throws Exception {
		
		Object output = null;
		
		XdbApi api = XdbUtil.getApi(apiName);
		
		XdbType<?> type = api.getType(serviceName, typeName);
		
		for (Object object : XdbUtil.createObjectList(type, search)) {
			System.out.println("*** REMOVE " + object);
			type.deleteObject(object);
			output = object;
		}
		
		/*
		DbManagerService service = AdbUtil.getService(serviceName);
		Class<?> type = AdbUtil.getType(service, typeName);
				
		String regName = service.getManager().getRegistryName(type);
		
		for (Object object : AdbUtil.getObjects(service, type, id)) {
		
			System.out.println("*** REMOVE " + object);
			service.getManager().deleteObject(regName, object);
			output = object;
		}
		*/
		if (outputParam != null)
			session.put(outputParam, output);
		return null;
	}
	

}
