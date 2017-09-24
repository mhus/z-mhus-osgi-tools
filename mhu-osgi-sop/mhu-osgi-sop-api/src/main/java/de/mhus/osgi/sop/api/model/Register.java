package de.mhus.osgi.sop.api.model;

import java.util.UUID;

import de.mhus.lib.adb.DbComfortableObject;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbPrimaryKey;
import de.mhus.lib.basics.UuidIdentificable;

public class Register extends DbComfortableObject implements UuidIdentificable {

	@DbPrimaryKey
	private UUID id;

	@DbPersistent
	@DbIndex({"a","b","c","d"})
	private String name;
	
	@DbPersistent
	@DbIndex({"a","b","c"})
	private String key1;
	@DbPersistent
	@DbIndex({"a","b"})
	private String key2;
	
	@DbPersistent
	@DbIndex({"a"})
	private String value1;
	@DbPersistent
	private String value2;
	
	
	public Register() {
	}
	
	public Register(String name, String key1, String key2, String value1, String value2) {
		super();
		this.name = name;
		this.key1 = key1;
		this.key2 = key2;
		this.value1 = value1;
		this.value2 = value2;
	}

	@Override
	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getKey1() {
		return key1;
	}

	public void setKey1(String key1) {
		this.key1 = key1;
	}

	public String getKey2() {
		return key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	};
	
	@Override
	public String toString() {
		return name + "," + key1 + "," + key2;
	}
	
}
