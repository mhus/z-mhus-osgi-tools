package de.mhus.karaf.xdb.cmd;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "xdb", name = "view", description = "Show a object")
@Service
public class CmdView implements Action {

	@Argument(index=0, name="service", required=true, description="Service Class", multiValued=false)
    String serviceName;

	@Argument(index=1, name="type", required=true, description="Type to select", multiValued=false)
    String typeName;
	
	@Argument(index=2, name="search", required=false, description="Id of the object or query in brakets e.g '($db.table.field$ = 1)'", multiValued=false)
    String search;
	
	@Option(name="-o", aliases="--out", description="Comma separated list of fields to print",required=false)
	String fieldsComma = null;

	@Option(name="-f", aliases="--full", description="Print the full value content also if it's very long",required=false)
	boolean full = false;

	@Option(name="-v", aliases="--verbose", description="Try to analyse Objects and print the values separately",required=false)
	boolean verbose = false;
	
	@Option(name="-m", aliases="--max", description="Maximum amount of chars for a value (if not full)",required=false)
	int max = 40;

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
		
		for (Object object : type.getObjects(search)) {
			
			System.out.println(">>> VIEW " + type.getIdAsString(object));

			ConsoleTable out = new ConsoleTable();
			out.setHeaderValues("Field","Value","Type");

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
			
			for (String name : fieldNames) {
				Object v = type.get(object, name);
				out.addRowValues(name,v,type.getAttributeType(name));
			}
			
			out.print(System.out);
			output = object;
			
		}
/*		
		DbManagerService service = AdbUtil.getService(serviceName);
		Class<?> type = AdbUtil.getType(service, typeName);
		
		String regName = service.getManager().getRegistryName(type);
		Table tableInfo = service.getManager().getTable(regName);
		
		ConsoleTable out = new ConsoleTable();
		out.setHeaderValues("Field","Value","Type");
		
		List<Field> pkeys = tableInfo.getPrimaryKeys();
		final HashSet<String> pkNames = new HashSet<>();
		for (Field f : pkeys)
			pkNames.add(f.getName());

		String[] fields = null;
		if (fieldsComma != null) fields = fieldsComma.split(",");

		LinkedList<Field> fieldList = new LinkedList<>();
		for (Field f : tableInfo.getFields())
			if (fields == null)
				fieldList.add(f);
			else {
				String fn = f.getName();
				for (String fn2 : fields) {
					if (fn2.equals(fn)) {
						fieldList.add(f);
						break;
					}
				}
			}
		
		Collections.sort(fieldList,new Comparator<Field>() {

			@Override
			public int compare(Field o1, Field o2) {
				boolean pk1 = pkNames.contains(o1.getName());
				boolean pk2 = pkNames.contains(o2.getName());
				if (pk1 == pk2)
					return o1.getName().compareTo(o2.getName());
				if (pk1) return -1;
				//if (pk2) return 1;
				return 1;
			}
		});
		
		for (Object object : AdbUtil.getObjects(service, type, id)) {

			System.out.println(">>> VIEW " + object);
			
			
			for (Field f : fieldList) {
				String name = f.getName();
				if (pkNames.contains(name)) name = name + "*";
				Object o = f.get(object);
				String value = null;
				if (verbose) {
					if (o == null) {
						value = "[null]";
					} else
					if (o instanceof Map) {
						// header
						out.addRowValues(">>> " + name,  "", f.getType().getSimpleName() );
						// data
						for (Entry<Object,Object> item : new TreeMap<Object,Object>( (Map<?,?>)o ).entrySet()) {
							String k = String.valueOf(item.getKey());
							String v = String.valueOf(item.getValue());
							if (!full && v.length() > max) v = MString.truncateNice(v, max);
							out.addRowValues(name + "." + k,  v, item.getValue() == null ? "null" : item.getValue().getClass().getSimpleName() );
						}
						// footer
						out.addRowValues("<<< " + name,  "", f.getType().getSimpleName() );
					} else
					if (o instanceof Collection) {
						// header
						out.addRowValues(">>> " + name,  "", f.getType().getSimpleName() );
						// data
						int cnt = 0;
						for (Object item : ((Collection<?>)object)) {
							String v = String.valueOf(item);
							if (!full && v.length() > max) v = MString.truncateNice(v, max);
							out.addRowValues(name + "[" + cnt + "]",  v, item == null ? "null" : item.getClass().getSimpleName() );
							cnt++;
						}
						// footer
						out.addRowValues("<<< " + name,  "", f.getType().getSimpleName() );
						
					} else {
						value = String.valueOf(o);
					}
				} else {
					value = String.valueOf(o);
				}
				if (value != null) {
					if (!full && value.length() > max) value = MString.truncateNice(value, max);
					out.addRowValues(name,  value, f.getType().getSimpleName() );
				}
			}
			out.print(System.out);
			output = object;
		}
		*/
		if (outputParam != null)
			session.put(outputParam, output);
		return null;
	}
	

}
