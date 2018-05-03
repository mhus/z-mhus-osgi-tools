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
package de.mhus.karaf.mongo.xdb;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.mongodb.morphia.annotations.Id;

import aQute.bnd.annotation.component.Component;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.DbComfortableObject;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.util.Table;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.lib.xdb.XdbType;

@Component(properties="xdb.type=mo")
public class MoXdbApi implements XdbApi {

	@Override
	public XdbService getService(String serviceName) throws NotFoundException {
		for (MoManagerService service : new TreeSet<MoManagerService>( MongoUtil.getManagerServices() ) )  {
			if (serviceName.equals(service.getServiceName()))
					return new Service(service);
		}
		throw new NotFoundException("Service not found",serviceName);
	}

	@Override
	public <T> XdbType<T> getType(String serviceName, String typeName) throws NotFoundException {

		for (MoManagerService service : new TreeSet<MoManagerService>( MongoUtil.getManagerServices() ) )  {
			if (serviceName.equals(service.getServiceName())) {

				String name = typeName.toLowerCase();
				Class<?> type = null;
				for (Class<? extends Persistable> s : service.getManager().getManagedTypes())
					if (s.getSimpleName().toLowerCase().equals(name)) {
						type = s;
						break;
					}
				if (type == null) throw new NotFoundException("Type not found",name,service.getServiceName());
				
				return new Type<T>(service,type);
			}
		}
		
		throw new NotFoundException("Service not found",serviceName);

	}

	@Override
	public List<String> getServiceNames() {
		LinkedList<String> out = new LinkedList<>();
		for (MoManagerService service : MongoUtil.getManagerServices() )  {
			out.add(service.getServiceName());
		}
		return out;
	}

	private static class Service implements XdbService {

		private MoManagerService service;

		public Service(MoManagerService service) {
			this.service = service;
		}

		@Override
		public boolean isConnected() {
			return service.isConnected();
		}

		@Override
		public List<String> getTypeNames() {
			LinkedList<String> out = new LinkedList<>();
			for (Class<? extends Persistable> s : service.getManager().getManagedTypes())
				out.add(s.getSimpleName());
			return out;
		}

		@Override
		public <T> XdbType<T> getType(String name) throws NotFoundException {
			name = name.toLowerCase();
			Class<?> type = null;
			for (Class<? extends Persistable> s : service.getManager().getManagedTypes())
				if (s.getSimpleName().toLowerCase().equals(name)) {
					type = s;
					break;
				}
			if (type == null) throw new NotFoundException("Type not found",name,service.getServiceName());
			
			return new Type<T>(service,type);
		}
		
		@Override
		public <T> XdbType<T> getType(Class<?> type) throws NotFoundException {
			Class<?> out = null;
			for (Class<? extends Persistable> s : service.getManager().getManagedTypes())
				if (s.getName().equals(type.getName())) {
					out = s;
					break;
				}
			if (out == null) throw new NotFoundException("Type not found",type,service.getServiceName());
			
			return new Type<T>(service,out);
		}


		@Override
		public String getSchemaName() {
			return service.getManager().getSchema().getClass().getName();
		}

		@Override
		public String getDataSourceName() {
			return service.getMongoDataSourceName();
		}

		@Override
		public void updateSchema(boolean cleanup) throws MException {
			// not supported, ignore
		}

		@Override
		public void connect() throws Exception {
			service.doOpen();
		}

		@Override
		public <T extends Persistable> T inject(T object) {
			if (object == null) return null;
			if (object instanceof DbComfortableObject)
				((DbComfortableObject)object).doInit(service.getManager(), null, false);
			return object;
		}

		@Override
		public <T> T getObject(Class<T> clazz, Object... keys) throws MException {
			return service.getManager().getObject(clazz, keys);
		}

		@Override
		public PojoModelFactory getPojoModelFactory() {
			return new PojoModelFactory() {
				
				@Override
				public PojoModel createPojoModel(Class<?> pojoClass) {
					try {
						return service.getManager().getModelFor(pojoClass);
					} catch (NotFoundException e) {
						throw new MRuntimeException(e);
					}
				}
			};
		}

		@Override
		public void delete(Persistable object) throws MException {
			service.getManager().delete(object);
		}

		@Override
		public void save(Persistable object) throws MException {
			service.getManager().save(object);
		}
		
	}

	private static class Type<T> implements XdbType<T> {

		private MoManagerService service;
		private Class<?> type;
		private PojoModel model;

		public Type(MoManagerService service, Class<?> type) throws NotFoundException {
			this.service = service;
			this.type = type;
			model = service.getManager().getModelFor(type);
		}

		@Override
		public DbCollection<T> getByQualification(String search, Map<String,Object> parameterValues) throws MException {
			try {
				return new Result<T>( MongoUtil.createQuery(service.getManager(), type, search, parameterValues).iterator() );
			} catch (IOException e) {
				throw new MException(e);
			}
		}

		@Override
		public DbCollection<T> getByQualification(AQuery<T> query) throws MException {
			try {
				return new Result<T>( MongoUtil.createQuery(service.getManager(), query).iterator() );
			} catch (IOException e) {
				throw new MException(e);
			}
		}

		@Override
		public List<String> getAttributeNames() {
			LinkedList<String> out = new LinkedList<>();
			for (PojoAttribute<?> f : model)
				out.add(f.getName());
			return out;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <F> F prepareManualValue(String name, Object value) {
			return (F) MongoUtil.prepareAttribute(getAttributeType(name), value);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void set(Object object, String name, Object v) throws MException {
			try {
				model.getAttribute(name).set(object, v);
			} catch (IOException e) {
				throw new MException(e);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <F> F get(Object object, String name) throws MException {
			try {
				return (F) model.getAttribute(name).get(object);
			} catch (IOException e) {
				throw new MException(e);
			}
		}

		@Override
		public void createObject(Object object) throws Exception {
			if (!type.isInstance(object))
				throw new NotSupportedException("Object wrong type",object.getClass(),type);
			service.getManager().createObject(null, object);
		}

		@Override
		public String getIdAsString(Object object) throws Exception {
			return String.valueOf(service.getManager().getId(object));
		}

		@Override
		public Object getId(Object object) throws MException {
			try {
				return service.getManager().getId(object);
			} catch (Exception e) {
				throw new MException(e);
			}
		}

		@Override
		public long count(String search, Map<String,Object> parameterValues) throws MException {
			try {
				return service.getManager().getCount(MongoUtil.createQuery(service.getManager(), type, search, parameterValues));
			} catch (IOException e) {
				throw new MException(e);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T newInstance() throws Exception {
			return service.getManager().inject((T)type.newInstance());
		}

		@Override
		public void deleteObject(Object object) throws MException {
			if (!type.isInstance(object))
				throw new NotSupportedException("Object wrong type",object.getClass(),type);
			service.getManager().delete(object);
		}

		@Override
		public Class<?> getAttributeType(String name) {
			PojoAttribute<?> a = model.getAttribute(name);
			if (a == null) return null;
			return a.getType();
		}

		@Override
		public boolean isPrimaryKey(String name) {
			PojoAttribute<?> a = model.getAttribute(name);
			if (a == null) return false;
			return a.getAnnotation(Id.class) != null;
		}

		@Override
		public boolean isPersistent(String name) {
			return true;
		}

		@Override
		public String getTechnicalName(String name) {
			return name;
		}

		@Override
		public void saveObjectForce(Object object, boolean raw) throws MException {
			saveObject(object);
		}

		@Override
		public void saveObject(Object object) throws MException {
			if (!type.isInstance(object))
				throw new NotSupportedException("Object wrong type",object.getClass(),type);
			service.getManager().save(object);
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getObject(String... keys) throws MException {
			return (T)service.getManager().getObject(type, (Object[])keys);
		}
		
	}
	
	private static class Result<O> implements DbCollection<O> {

		private Iterator<O> iterator;
		private O current;

		@SuppressWarnings("unchecked")
		public Result(Iterator<?> iterator) {
			this.iterator = (Iterator<O>) iterator;
		}

		@Override
		public Iterator<O> iterator() {
			return iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public O next() {
			current = iterator.next();
			return current;
		}

		@Override
		public void close() {
		}

		@Override
		public DbCollection<O> setRecycle(boolean on) {
			return null;
		}

		@Override
		public boolean isRecycle() {
			return false;
		}

		@Override
		public O current() throws MException {
			return current;
		}

		@Override
		public Table toTableAndClose(int maxSize) {
			return null;
		}
		
	}
	
}
