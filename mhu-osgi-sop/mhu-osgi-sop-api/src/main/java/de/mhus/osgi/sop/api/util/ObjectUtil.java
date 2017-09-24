package de.mhus.osgi.sop.api.util;

import java.util.List;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.core.MApi;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.model.ObjectParameter;

public class ObjectUtil {
	
	public static ObjectParameter getParameter(Class<?> type, UUID id, String key) throws MException {
		return MApi.lookup(AdbApi.class).getParameter(type, id, key);
	}

	public static void setParameter(Class<?> type, UUID id, String key, String value) throws MException {
		MApi.lookup(AdbApi.class).setParameter(type, id, key, value);
	}

	public static void deleteAll(Class<?> type, UUID id) throws MException {
		MApi.lookup(AdbApi.class).deleteParameters(type, id);
	}

	public static List<ObjectParameter> getParameters(Class<?> type, String key, String value) throws MException {
		return MApi.lookup(AdbApi.class).getParameters(type, key, value);
	}

	public static String getRecursiveValue(Class<? extends DbMetadata> clazz, UUID id, String key, String def) throws MException {
		DbMetadata obj = MApi.lookup(AdbApi.class).getObject(clazz, id);
		return getRecursiveValue(obj, key, def);
	}
	
	public static String getRecursiveValue(DbMetadata obj, String key, String def) {
		ObjectParameter out = null;
		try {
			out = MApi.lookup(AdbApi.class).getRecursiveParameter(obj, key);
		} catch (MException e) {
			
		}
		 if (out == null || out.getValue() == null) return def;
		 return out.getValue();
	}	

}
