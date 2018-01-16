package de.mhus.karaf.xdb.adb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.InvalidSyntaxException;

import aQute.bnd.annotation.component.Component;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.karaf.xdb.model.XdbService;
import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.model.Field;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.adb.AdbUtil;
import de.mhus.lib.karaf.adb.DbManagerService;

@Component(properties="xdb.type=adb")
public class AdbXdbApi implements XdbApi {

	public static final String NAME = "adb";

	@Override
	public XdbService getService(String serviceName) throws NotFoundException {
		try {
			DbManagerService service = AdbUtil.getService(serviceName);
			
			return new Service(service);
			
		} catch (IOException | InvalidSyntaxException e) {
			throw new NotFoundException("Service not found",serviceName, e);
		}
	}

	@Override
	public <T> XdbType<T> getType(String serviceName, String typeName) throws NotFoundException {
		try {
			DbManagerService service = AdbUtil.getService(serviceName);
			
			String tableName = AdbUtil.getTableName(service, typeName);
			Table table = service.getManager().getTable(tableName);
			if (table == null) throw new NotFoundException("Table not found",typeName,service.getServiceName());
			return new Type<T>(service,table);

		} catch (IOException | InvalidSyntaxException e) {
			throw new NotFoundException("Service not found",serviceName, e);
		}
	}

	@Override
	public List<String> getServiceNames() {
		LinkedList<String> out = new LinkedList<>();
		for (DbManagerService s : AdbUtil.getServices(false)) {
			out.add(s.getServiceName());
		}
		return out;
	}

	private static class Service implements XdbService {

		private DbManagerService service;

		public Service(DbManagerService service) {
			this.service = service;
		}

		@Override
		public boolean isConnected() {
			return service.isConnected();
		}

		@Override
		public List<String> getTypeNames() {
			LinkedList<String> out = new LinkedList<>();
			for (Class<? extends Persistable> o : service.getManager().getSchema().getObjectTypes())
				out.add(o.getSimpleName());
			return out;
		}

		@Override
		public <T> XdbType<T> getType(Class<?> type) throws NotFoundException {
			String tableName;
			try {
				tableName = AdbUtil.getTableName(service,type);
			} catch (IOException e) {
				throw new NotFoundException("Table not found",type,service.getServiceName());
			}
			Table table = service.getManager().getTable(tableName);
			if (table == null) throw new NotFoundException("Table not found",type,service.getServiceName());
			return new Type<T>(service,table);
		}
		
		@Override
		public <T> XdbType<T> getType(String name) throws NotFoundException {
			String tableName;
			try {
				tableName = AdbUtil.getTableName(service,name);
			} catch (IOException e) {
				throw new NotFoundException("Table not found",name,service.getServiceName());
			}
			Table table = service.getManager().getTable(tableName);
			if (table == null) throw new NotFoundException("Table not found",name,service.getServiceName());
			return new Type<T>(service,table);
		}

		@Override
		public String getSchemaName() {
			return service.getManager().getSchema().getSchemaName();
		}

		@Override
		public String getDataSourceName() {
			return service.getDataSourceName();
		}
		
		@Override
		public void updateSchema(boolean cleanup) throws Exception {
			service.updateManager(cleanup);
		}

		@Override
		public void connect() throws Exception {
			service.getManager().connect();
		}

	}
	
	private static class Type<T> implements XdbType<T> {

		private DbManagerService service;
		private Table table;

		public Type(DbManagerService service, Table table) {
			this.service = service;
			this.table = table;
			
			
		}

		@SuppressWarnings("unchecked")
		@Override
		public DbCollection<T> getByQualification(String search, Map<String,Object> parameterValues) throws MException {
			return (DbCollection<T>) service.getManager().getByQualification(table.getClazz(),search, parameterValues);
		}

		@Override
		public DbCollection<T> getByQualification(AQuery<T> query) throws Exception {
			return (DbCollection<T>) service.getManager().getByQualification(query);
		}

		@Override
		public List<String> getAttributeNames() {
			LinkedList<String> out = new LinkedList<>();
			for (Field f : table.getFields())
				out.add(f.getName());
			return out;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <F> F prepareManualValue(String name, Object value) {
			return (F) AdbUtil.createAttribute(table.getField(name).getType(), value);
		}

		@Override
		public void set(Object object, String name, Object v) throws Exception {
			table.getField(name).set(object, v);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <F> F get(Object object, String name) throws Exception {
			return (F) table.getField(name).get(object);
		}

		@Override
		public void createObject(Object object) throws MException {
			service.getManager().createObject(object);
		}

		@Override
		public String getIdAsString(Object object) throws Exception {
			StringBuffer out = new StringBuffer();
			for (Field f : table.getPrimaryKeys()) {
				if (out.length() > 0) out.append(",");
				out.append(f.get(object));
			}

			return out.toString();
		}

		@Override
		public long count(String search, Map<String,Object> parameterValues) throws MException {
			return service.getManager().getCountByQualification(table.getClazz(), search, parameterValues);
		}

		@Override
		public T newInstance() throws Exception {
			@SuppressWarnings("unchecked")
			T out = (T) table.getClazz().newInstance();
			service.getManager().inject((Persistable) out);
			return out;
		}

		@Override
		public void deleteObject(Object object) throws MException {
			service.getManager().deleteObject(object);
		}

		@Override
		public Class<?> getAttributeType(String name) {
			return table.getField(name).getType();
		}

		@Override
		public boolean isPrimaryKey(String name) {
			for (Field f : table.getPrimaryKeys())
				if (f.getName().equals(name)) return true;
			return false;
		}

		@Override
		public boolean isPersistent(String name) {
			return table.getField(name).isPersistent();
		}

		@Override
		public String getTechnicalName(String name) {
			return table.getField(name).getMappedName();
		}

		@Override
		public void saveObjectForce(Object object, boolean raw) throws MException {
			service.getManager().saveObjectForce(object, raw);
		}

		@Override
		public void saveObject(Object object) throws MException {
			service.getManager().saveObject(object);
		}

		@Override
		public Object getId(Object object) throws Exception {
			List<Field> pk = table.getPrimaryKeys();
			if (pk.size() < 1) return null;
			if (pk.size() == 1) return pk.get(0).get(object);
			
			Object[] out = new Object[pk.size()];
			int cnt = 0;
			for (Field f : pk) {
				out[cnt] = f.get(object);
				cnt++;
			}

			return out;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getObject(String... keys) throws Exception {
			return (T) service.getManager().getObject(table.getClazz(), keys);
		}
		
	}
}
