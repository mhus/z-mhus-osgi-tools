/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.mongo;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.JsonProcessingException;
import org.mongodb.morphia.query.Query;

import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.mongo.MoManager;
import de.mhus.osgi.services.MOsgi;

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


	public static <T> Query<T> createQuery(MoManager manager, Class<T> type, String search, Map<String,Object> parameterValues) throws JsonProcessingException, IOException {
		Query<T> q = manager.createQuery(type);
		if (MString.isSet(search)) {
			new MoQueryBuilder(search).create(q, parameterValues);
		}
		return q;
	}
	
	public static <T> Query<T> createQuery(MoManager manager, AQuery<T> query) throws IOException {
		@SuppressWarnings("unchecked")
		Query<T> q = (Query<T>) manager.createQuery(query.getType());
		new MoQueryBuilder(query).create(q,null);
		return q;
	}
	
}
