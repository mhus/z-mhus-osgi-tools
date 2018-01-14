package de.mhus.karaf.mongo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.mongodb.morphia.annotations.Id;

import aQute.bnd.annotation.component.Component;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbService;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.util.Table;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotSupportedException;

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
		public String getSchemaName() {
			return service.getManager().getSchema().getClass().getName();
		}

		@Override
		public String getDataSourceName() {
			return service.getMongoDataSourceName();
		}

		@Override
		public void updateSchema(boolean cleanup) throws Exception {
			// not supported, ignore
		}

		@Override
		public void connect() throws Exception {
			service.doOpen();
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
		public DbCollection<T> getByQualification(String search, Map<String,Object> parameterValues) throws Exception {
			return new Result<T>( MongoUtil.createQuery(service.getManager(), type, search, parameterValues).iterator() );
		}

		@Override
		public DbCollection<T> getByQualification(AQuery<T> query) throws Exception {
			return new Result<T>( MongoUtil.createQuery(service.getManager(), query).iterator() );
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
		public void set(Object object, String name, Object v) throws Exception {
			model.getAttribute(name).set(object, v);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <F> F get(Object object, String name) throws Exception {
			return (F) model.getAttribute(name).get(object);
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
		public Object getId(Object object) throws Exception {
			return service.getManager().getId(object);
		}

		@Override
		public long count(String search, Map<String,Object> parameterValues) throws Exception {
			return service.getManager().getCount(MongoUtil.createQuery(service.getManager(), type, search, parameterValues));
		}

		@SuppressWarnings("unchecked")
		@Override
		public T newInstance() throws Exception {
			return service.getManager().inject((T)type.newInstance());
		}

		@Override
		public void deleteObject(Object object) throws Exception {
			if (!type.isInstance(object))
				throw new NotSupportedException("Object wrong type",object.getClass(),type);
			service.getManager().delete(object);
		}

		@Override
		public Class<?> getAttributeType(String name) {
			return model.getAttribute(name).getType();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean isPrimaryKey(String name) {
			return model.getAttribute(name).getAnnotation(Id.class) != null;
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
		public void saveObjectForce(Object object, boolean raw) throws Exception {
			saveObject(object);
		}

		@Override
		public void saveObject(Object object) throws Exception {
			if (!type.isInstance(object))
				throw new NotSupportedException("Object wrong type",object.getClass(),type);
			service.getManager().save(object);
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getObject(String... keys) throws Exception {
			return (T) service.getManager().getObject(type, keys);
		}
		
	}
	
	private static class Result<O> implements DbCollection<O> {

		private Iterator<O> iterator;
		private O current;

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
