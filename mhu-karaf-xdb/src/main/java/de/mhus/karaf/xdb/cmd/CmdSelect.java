package de.mhus.karaf.xdb.cmd;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;

@Command(scope = "xdb", name = "select", description = "Select data from DB DataSource ant print the results")
@Service
public class CmdSelect implements Action {

	@Argument(index=0, name="type", required=true, description="Type to select", multiValued=false)
    String typeName;
	
	@Argument(index=1, name="qualification", required=false, description="Select qualification", multiValued=false)
    String qualification;

	@Option(name="-f", aliases="--full", description="Print the full value content also if it's very long",required=false)
	boolean full = false;

	@Option(name="-l", aliases="--oneline", description="Disable one line",required=false)
	boolean oneLine = false;
	
	@Option(name="-m", aliases="--max", description="Maximum amount of chars for a value (if not full)",required=false)
	int max = 40;

	@Option(name="-o", aliases="--out", description="Comma separated list of fields to print",required=false)
	String fieldsComma = null;
	
	@Option(name="-x", description="Output parameter",required=false)
	String outputParam = null;
	
	@Option(name="-a", description="Api Name",required=false)
	String apiName = CmdUse.api;

	@Option(name="-s", description="Service Name",required=false)
	String serviceName = CmdUse.service;

	@Option(name="-v", aliases="--csv", description="CSV Style",required=false)
	boolean csv = false;

    @Reference
    private Session session;

	@Override
	public Object execute() throws Exception {
		
		Object output = null;
		
		XdbApi api = XdbUtil.getApi(apiName);
		XdbType<?> type = api.getType(serviceName, typeName);
		
		// sort columns to print
		final LinkedList<String> fieldNames = new LinkedList<>();
		if (fieldsComma == null) {
			for (String name : type.getAttributeNames()) {
					fieldNames.add(name);
			}
			
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
			
			
		} else {
			for (String name : fieldsComma.split(","))
				fieldNames.add(name);
		}
		
		ConsoleTable out = new ConsoleTable();
		if (csv) {
			out.setColSeparator(";");
			out.setCellSpacer(false);
		}
		if (oneLine)
			out.setMultiLine(false);
		if (!full)
			out.setMaxColSize(max);
		for (String name : fieldNames) {
			if (type.isPrimaryKey(name)) name = name + "*";
			out.getHeader().add(name);
		}

		for (Object object : type.getByQualification(qualification, null)) {
			
			ConsoleTable.Row row = out.addRow();
			for (String name : fieldNames) {
				Object value = toValue( type.get(object, name) );
				row.add(value);
			}
			output = object;
		}

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
		
		
		ConsoleTable out = new ConsoleTable();
		for (Field f : fieldList) {
			String name = f.getName();
			if (pkNames.contains(name)) name = name + "*";
			out.getHeader().add(name);
		}
		DbCollection<?> res = service.getManager().getByQualification(type, qualification, attrObj);
		
		for (Object item : res) {
			List<String> row = out.addRow();
			for (Field f : fieldList) {
				String value = toString(f.get(item));
				if (!full && value.length() > max) value = MString.truncateNice(value, max);
				row.add(value);
			}
			output = item;
		}
		res.close();
*/		
		out.print(System.out);
		
		if (outputParam != null)
			session.put(outputParam, output);
		return null;
	}

	private Object toValue(Object object) {
		if (object == null) return "[null]";
		return object;
	}
	

}
