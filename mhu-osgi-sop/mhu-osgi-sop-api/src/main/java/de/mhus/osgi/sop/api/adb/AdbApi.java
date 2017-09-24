package de.mhus.osgi.sop.api.adb;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import de.mhus.lib.adb.DbManager;
import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SApi;
import de.mhus.osgi.sop.api.model.ActionTask;
import de.mhus.osgi.sop.api.model.ObjectParameter;

public interface AdbApi extends SApi {

	int PAGE_SIZE = 100;

	DbManager getManager();

	ActionTask createActionTask(String queue, String action, String target, String[] properties, boolean smart) throws MException;

	List<ActionTask> getQueue(String queue, int max) throws MException;

	List<ObjectParameter> getParameters(Class<?> type, UUID id)
			throws MException;

	List<ObjectParameter> getParameters(String type, UUID id) throws MException;

	void setGlobalParameter(String key, String value) throws MException;

	void setParameter(Class<?> type, UUID id, String key, String value)
			throws MException;

	void setParameter(String type, UUID id, String key, String value)
			throws MException;

	ObjectParameter getGlobalParameter(String key) throws MException;

	String getValue(String type, UUID id, String key, String def)
			throws MException;

	String getValue(Class<?> type, UUID id, String key, String def)
			throws MException;

	ObjectParameter getParameter(String type, UUID id, String key)
			throws MException;

	ObjectParameter getParameter(Class<?> type, UUID id, String key)
			throws MException;

	void deleteParameters(Class<?> type, UUID id) throws MException;

	List<ObjectParameter> getParameters(Class<?> type, String key, String value) throws MException;

	<T> LinkedList<T> collectResults(AQuery<T> asc, int page) throws MException;

	ObjectParameter getRecursiveParameter(DbMetadata obj, String key) throws MException;

	List<ActionTask> getActionTaskPage(String queue, int size);
	
	boolean canRead(DbMetadata obj) throws MException;
	boolean canUpdate(DbMetadata obj) throws MException;
	boolean canDelete(DbMetadata obj) throws MException;
//	boolean canCreate(Object parent, String newType) throws MException;
//	boolean canCreate(Object parent, Class<?> newType) throws MException;
	boolean canCreate(DbMetadata obj) throws MException;

	<T extends DbMetadata> T getObject(Class<T> type, UUID id) throws MException;
	<T extends DbMetadata> T getObject(String type, UUID id) throws MException;
	<T extends DbMetadata> T getObject(String type, String id) throws MException;
	
	Set<Entry<String, DbSchemaService>> getController();

	void onDelete(DbMetadata object);

	void collectRefereces(DbMetadata object, ReferenceCollector collector);

}
