package de.mhus.karaf.xdb.model;

import java.util.List;
import java.util.Map;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.errors.MException;

public interface XdbType<T> {

	/**
	 * Search and return objects from db
	 * 
	 * @param query
	 * @param parameterValues A list of parameter values or null if not needed
	 * @return a result stream. You need to close the result or iterate it until the end (automatic close)
	 * @throws Exception
	 */
	DbCollection<T> getByQualification(String query, Map<String,Object> parameterValues) throws Exception;

	/**
	 * Search and return objects from db
	 * 
	 * @param query Adb.query()....
	 * @return a result stream. You need to close the result or iterate it until the end (automatic close)
	 * @throws Exception
	 */
	DbCollection<T> getByQualification(AQuery<T> query) throws Exception;
	
	/**
	 * List all known attribute names
	 * @return All known attribute names.
	 */
	List<String> getAttributeNames();

	/**
	 * Prepare a value to be stored in a object attribute. Use case is a manually inserted
	 * value by the user.
	 * 
	 * @param attributeName The name of the attribute
	 * @param value The initial value or null (null or '[null]' will return null)
	 * @return A prepared value of this attribute using the initial value
	 */
	<F> F prepareManualValue(String attributeName, Object value);

	/**
	 * Set the value of an attribute to the object.
	 * 
	 * @param object
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	void set(Object object, String name, Object value) throws Exception;
	
	/**
	 * Load/get the value of an attribute of the object.
	 * 
	 * @param object
	 * @param name
	 * @return
	 * @throws Exception
	 */
	<F> F get(Object object, String name) throws Exception;

	/**
	 * Create a new object. If the object was already persistent a new clone will be created.
	 * 
	 * @param object
	 * @throws Exception
	 */
	void createObject(Object object) throws Exception;

	/**
	 * Returns the id of the object as string representation.
	 * 
	 * @param object
	 * @return A string representation of the primary key or id
	 * @throws Exception 
	 */
	String getIdAsString(Object object) throws Exception;

	/**
	 * Returns the id of the object as object, could also be an array if a combined
	 * primary key is used.
	 * 
	 * @param object
	 * @return The primary key or id. If it's a combined primary key a object array will be returned.
	 * @throws Exception 
	 */
	Object getId(Object object) throws Exception;
	
	/**
	 * Return the amount of objects in the database.
	 * 
	 * @param query
	 * @param parameterValues 
	 * @return count the objects by query
	 * @throws Exception
	 */
	long count(String query, Map<String,Object> parameterValues) throws Exception;

	/**
	 * Create a new instance of the type.
	 * 
	 * @return
	 * @throws Exception
	 */
	T newInstance() throws Exception;

	/**
	 * Delete the persistent representation of the object.
	 * 
	 * @param object
	 * @throws Exception
	 */
	void deleteObject(Object object) throws Exception;

	/**
	 * Returns the type of the attribute.
	 * 
	 * @param name
	 * @return
	 */
	Class<?> getAttributeType(String name);

	/**
	 * Returns true if the attribute is a primary key.
	 * 
	 * @param name
	 * @return
	 */
	boolean isPrimaryKey(String name);

	/**
	 * Returns true if the attribute will be stored in database.
	 * 
	 * @param name
	 * @return
	 */
	boolean isPersistent(String name);

	/**
	 * Returns a technical or mapped name of the attribute (most time the same as the name).
	 * 
	 * @param name
	 * @return
	 */
	String getTechnicalName(String name);

	/**
	 * Force the save of the object. If the method is not supported it should save the object
	 * in a default way.
	 * 
	 * @param object
	 * @param raw Do not fire events.
	 * @throws Exception
	 */
	void saveObjectForce(Object object, boolean raw) throws Exception;

	/**
	 * Store the object in the database. If the object is not already persistent it will
	 * be created.
	 * 
	 * @param object
	 * @throws Exception
	 */
	void saveObject(Object object) throws Exception;

	/**
	 * Return the requested object by primary key.
	 * 
	 * @param key Primary key
	 * @return The corresponding object or null
	 * @throws Exception 
	 */
	T getObject(String ... keys) throws Exception;

}
