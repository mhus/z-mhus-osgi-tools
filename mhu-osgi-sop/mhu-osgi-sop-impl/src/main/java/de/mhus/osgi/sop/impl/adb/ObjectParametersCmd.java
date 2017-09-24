package de.mhus.osgi.sop.impl.adb;

import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.model.ObjectParameter;
import de.mhus.osgi.sop.api.util.ObjectUtil;

@Command(scope = "mhus", name = "parameter", description = "Handle object parameters")
@Service
public class ObjectParametersCmd implements Action {

	@Argument(index=0, name="type", required=true, description="Type of the object", multiValued=false)
    String type;

	@Argument(index=1, name="id", required=true, description="Object's UUID or - for the empty id (global)", multiValued=false)
    String id;

	@Argument(index=2, name="cmd", required=true, description="Command: list, set, remove, clean, get, recusive", multiValued=false)
    String cmd;
	
	@Argument(index=3, name="params", required=false, description="Parameters to set key=value", multiValued=true)
    String[] params;
	
	@Override
	public Object execute() throws Exception {

		DbManager manager = MApi.lookup(AdbApi.class).getManager();
				
		if (type.equals("parameter")) {
			
			switch(cmd) {
			case "list": {
	
				ConsoleTable out = new ConsoleTable();
				out.setHeaderValues("Key","Type","Object","Value","Id");
				
				for (ObjectParameter p : manager.getByQualification(Db.query(ObjectParameter.class).eq(Db.attr("key"), Db.value(id))))
					out.addRowValues( p.getKey(), p.getObjectType(), p.getObjectId(), p.getValue(), p.getId() );
				
				out.print(System.out);
			} break;
			}
			return null;
		} 
		
		Class<?> cType = null;
		UUID rId = MConstants.EMPTY_UUID;
		if (!"-".equals(id)) {
			DbMetadata obj = MApi.lookup(AdbApi.class).getObject(type, id);
			if (obj == null) {
				System.out.println("Object not found");
				return null;
			}
			
			cType = obj.getClass();
			rId = obj.getId();
		} else {
		}
		
		switch(cmd) {
		case "list": {
			ConsoleTable table = new ConsoleTable();
			table.setHeaderValues("KEY","VALUE");

			for (ObjectParameter param : MApi.lookup(AdbApi.class).getParameters(cType, rId)) {
				table.addRowValues(param.getKey(),param.getValue());
			}
			table.print(System.out);
		} break;
		case "set": {
			for (String pair : params) {
				String key = MString.beforeIndex(pair, '=');
				String value = MString.afterIndex(pair, '=');
				ObjectUtil.setParameter(cType,rId,key, value);
			}
			System.out.println("OK");
		} break;
		case "remove": {
			for (String p : params) {
				ObjectParameter pp = MApi.lookup(AdbApi.class).getParameter(cType, rId, p);
				if (pp != null)
					pp.delete();
				else
					System.out.println("Parameter not found: " + p);
			}
			System.out.println("OK");
		} break;
		case "clean": {
			for (ObjectParameter param : MApi.lookup(AdbApi.class).getParameters(cType, rId)) {
				param.delete();
			}
			System.out.println("OK");
		} break;
		case "get": {
			ObjectParameter p = MApi.lookup(AdbApi.class).getParameter(cType, rId, params[0]);
			if (p != null)
				System.out.println(p.getValue());
		} break;
		case "recursive": {
			ObjectParameter p = MApi.lookup(AdbApi.class).getParameter(cType, rId, params[0]);
			if (p != null)
				System.out.println(p.getValue());
		} break;
		}
		
		
		return null;
	}

}
