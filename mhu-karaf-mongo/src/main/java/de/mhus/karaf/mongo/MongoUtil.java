package de.mhus.karaf.mongo;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Serialized;
import org.mongodb.morphia.query.Query;

import de.mhus.lib.annotations.pojo.Action;
import de.mhus.lib.annotations.pojo.Hidden;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.pojo.AttributesStrategy;
import de.mhus.lib.core.pojo.DefaultFilter;
import de.mhus.lib.core.pojo.FunctionsStrategy;
import de.mhus.lib.core.pojo.PojoAction;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoFilter;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoModelImpl;
import de.mhus.lib.core.pojo.PojoParser;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.lib.mongo.MoManager;

public class MongoUtil {

	public static MongoDataSource getDatasource(String name) throws NotFoundException {
		return MOsgi.getService(MongoDataSource.class, "(lookup.name=" + name + ")");
	}

	public static List<MoManagerService> getManagerServices() {
		return MOsgi.getServices(MoManagerService.class, null);
	}

	public static Object prepareAttribute(Class<?> type, Object value) {

		//TODO use escape -ing
		if (value == null || value.equals("[null]")) return null;
		if (value.equals("[uuid]")) return UUID.randomUUID();
		
		if (value instanceof String) {
			String str = (String)value;
			if (str.startsWith("[") && str.endsWith("]")) {
				String[] parts = str.substring(1, str.length()-1).split(",");
				for (int i = 0; i < parts.length; i++)
					parts[i] = MUri.decode(parts[i]);
				value = parts;
			} else {
				value = MUri.decode(str);
			}
		}
		
		if (type == value.getClass()) return value;
		
		if (type == int.class || type == Integer.class)
			return MCast.toint(value, 0);
		
		if (type == long.class || type == Long.class)
			return MCast.tolong(value, 0);
		
		if (type == float.class || type == Float.class)
			return MCast.tofloat(value, 0);

		if (type == double.class || type == Double.class)
			return MCast.todouble(value, 0);

		if (type == boolean.class || type == Boolean.class)
			return MCast.toboolean(value, false);
		
		if (type == Date.class )
			return MCast.toDate(String.valueOf(value), null);
		
		if (type == java.sql.Date.class ) {
			Date data = MCast.toDate(String.valueOf(value), null);
			if (data == null) return null;
			return new java.sql.Date( data.getTime() );
		}
		
		if (type == UUID.class )
			return UUID.fromString(String.valueOf(value));
		
		if (type.isEnum())
			return String.valueOf(value);
		
		return null;
	}

	public static PojoModel getPojoModel(Class<?> type) {
		PojoModel model = new PojoParser().parse(type, new AttributesStrategy()).filter(new MongoFilter()).getModel();
		return model;
	}

	private static class MongoFilter implements PojoFilter {

		@Override
		public void filter(PojoModelImpl model) {
			for (String name : model.getAttributeNames()) {
				PojoAttribute<?> attr = model.getAttribute(name);
				if (
						
						attr.getAnnotation(NotSaved.class) != null 
						||
						( !attr.getType().isPrimitive() || attr.getType() == String.class || attr.getType() == Date.class  )
						&&
						attr.getAnnotation(Property.class) == null 
						&& 
						attr.getAnnotation(Id.class) == null
						&&
						attr.getAnnotation(Serialized.class) == null
						&&
						attr.getAnnotation(Reference.class) == null
						&&
						attr.getAnnotation(Embedded.class) == null
						
						) {
					model.removeAttribute(name);
				}
			}
			
			for (String name : model.getActionNames()) {
				model.removeAction(name);
			}
			
		}
		
	}

	public static <T> Query<T> createQuery(MoManager manager, Class<T> type, String search) {
		Query<T> q = manager.createQuery(type);
		if (MString.isSet(search)) {
			q.where(search);
		}
		return q;
	}
}
