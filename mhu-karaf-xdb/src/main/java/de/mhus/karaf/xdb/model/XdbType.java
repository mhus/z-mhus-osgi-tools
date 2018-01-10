package de.mhus.karaf.xdb.model;

import java.util.List;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.errors.MException;

public interface XdbType<T> {

	/**
	 * Search and return objects from db
	 * 
	 * @param search
	 * @return
	 * @throws Exception
	 */
	DbCollection<T> getObjects(String search) throws Exception;

	/**
	 * List all known attribute names
	 * @return
	 */
	List<String> getAttributeNames();

	/**
	 * Prepare a value to be stored in a object attribute
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	<F> F prepareValue(String name, Object value);

	/**
	 * Set the value of an attribute.
	 * 
	 * @param object
	 * @param name
	 * @param v
	 * @throws Exception
	 */
	void set(Object object, String name, Object v) throws Exception;
	
	/**
	 * Load/get the value of an attribute.
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
	 * @return
	 * @throws Exception 
	 */
	String getIdAsString(Object object) throws Exception;

	/**
	 * Returns the id of the object as object, could also be an array if a combined
	 * primary key is used.
	 * 
	 * @param object
	 * @return
	 * @throws Exception 
	 */
	Object getId(Object object) throws Exception;
	
	/**
	 * Return the amount of objects in the database.
	 * 
	 * @param search
	 * @return
	 * @throws Exception
	 */
	long count(String search) throws Exception;

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
