package de.mhus.osgi.sop.api.util;

import java.util.List;
import java.util.UUID;

import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.model.DbMetadata;
import de.mhus.osgi.sop.api.model.ObjectParameter;

public class ObjectUtil {
	
	public static ObjectParameter getParameter(Class<?> type, UUID id, String key) throws MException {
		return Sop.getApi(AdbApi.class).getParameter(type, id, key);
	}

	public static void setParameter(Class<?> type, UUID id, String key, String value) throws MException {
		Sop.getApi(AdbApi.class).setParameter(type, id, key, value);
	}

	public static void deleteAll(Class<?> type, UUID id) throws MException {
		Sop.getApi(AdbApi.class).deleteParameters(type, id);
	}

	public static List<ObjectParameter> getParameters(Class<?> type, String key, String value) throws MException {
		return Sop.getApi(AdbApi.class).getParameters(type, key, value);
	}

	public static String getRecursiveValue(Class<? extends DbMetadata> clazz, UUID id, String key, String def) throws MException {
		DbMetadata obj = Sop.getApi(AdbApi.class).getObject(clazz, id);
		return getRecursiveValue(obj, key, def);
	}
	
	public static String getRecursiveValue(DbMetadata obj, String key, String def) {
		ObjectParameter out = null;
		try {
			out = Sop.getApi(AdbApi.class).getRecursiveParameter(obj, key);
		} catch (MException e) {
			
		}
		 if (out == null || out.getValue() == null) return def;
		 return out.getValue();
	}	

}
