package de.mhus.karaf.xdb.cmd;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.lib.karaf.MOsgi.Service;

public class XdbUtil {

	public static XdbApi getApi(String apiName) throws NotFoundException {
		XdbApi api = MOsgi.getService(XdbApi.class, "(xdb.type="+apiName+")");
		if (api == null) throw new NotFoundException("Command API not found",apiName);
		return api;
	}

	public static List<String> getApis() {
		LinkedList<String> out = new LinkedList<>();
		for (Service<XdbApi> s : MOsgi.getServiceRefs(XdbApi.class, null))
			out.add(String.valueOf(s.getReference().getProperty("xdb.type")));
		return out;
	}

	public static <T> DbCollection<T> createObjectList(XdbType<T> type, String search) throws Exception {
		
		if (search.startsWith("(") && search.endsWith(")"))
			return type.getObjects(search.substring(1, search.length()-1));
		
		return new IdArrayCollection<T>(type, search.split(","));
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setValue(XdbType<?> type, Object object, String name, Object v) throws Exception {
		int p = name.indexOf('.');
		if (p > 0) {
			String p1 = name.substring(0, p);
			String p2 = name.substring(p+1);
			Class<?> t = type.getAttributeType(p1);
			if (t.isAssignableFrom(Map.class)) {
				Map map = type.get(object, p1);
				if (map == null) {
					if (t.isInterface())
						map = new HashMap<>();
					else
						map = (Map) t.newInstance();
					type.set(object, p1, map);
				}
				map.put(p2, v);
			} else
			if (t.isAssignableFrom(Collection.class)) {
				Collection col = type.get(object, name);
				if (col == null) {
					if (t.isInterface())
						col = new LinkedList<>();
					else
						col = (Collection) t.newInstance();
					type.set(object, p1, col);
				}
				if (p2.equals("add") || p2.equals("last")) {
					col.add(v);
				} else
				if (p2.equals("first") && col instanceof Deque) {
					((Deque)col).addFirst(v);
				} else
				if (p2.equals("clear")) {
					col.clear();
				} else
				if (p2.equals("remove")) {
					col.remove(v);
				} else {
					int i = MCast.toint(p2, -1);
					if (i > -1 && col instanceof AbstractList) {
						((AbstractList)col).set(i, v);
					}
				}
				
			}
		} else
			type.set(object, name, v);
	}

	public static Object prepareValue(XdbType<?> type, String name, Object value) {
		int p = name.indexOf('.');
		if (p > 0) return value;
		Object v = type.prepareValue(name, value);
		return v;
	}

}
