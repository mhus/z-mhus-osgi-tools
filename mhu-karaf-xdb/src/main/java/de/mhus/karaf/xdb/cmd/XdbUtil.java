package de.mhus.karaf.xdb.cmd;

import java.util.LinkedList;
import java.util.List;

import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.DbCollection;
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

}
