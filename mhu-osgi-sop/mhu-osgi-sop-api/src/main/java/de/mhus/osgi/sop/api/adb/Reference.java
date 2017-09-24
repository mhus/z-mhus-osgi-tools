package de.mhus.osgi.sop.api.adb;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.core.MLog;

public class Reference<T extends DbMetadata> extends MLog {

	public enum TYPE {CHILD,PARENT,OTHER}
	private T object;
	private TYPE type;

	public Reference(T object, TYPE type) {
		this.object = object;
		this.type = type;
	}
	
	public T getObject() {
		return object;
	}

	public TYPE getType() {
		return type;
	}
	
}
