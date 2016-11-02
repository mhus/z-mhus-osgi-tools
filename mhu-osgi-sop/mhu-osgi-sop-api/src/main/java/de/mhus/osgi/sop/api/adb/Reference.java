package de.mhus.osgi.sop.api.adb;

import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.model.DbMetadata;

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
