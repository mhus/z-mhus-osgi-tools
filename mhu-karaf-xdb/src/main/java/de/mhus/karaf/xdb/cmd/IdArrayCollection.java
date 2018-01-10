package de.mhus.karaf.xdb.cmd;

import java.util.Iterator;

import de.mhus.karaf.xdb.model.XdbType;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.util.Table;
import de.mhus.lib.errors.MException;

public class IdArrayCollection<T> implements DbCollection<T> {

	private XdbType<T> type;
	private String[] array;
	private int index;
	private T current;

	public IdArrayCollection(XdbType<T> type, String[] array) {
		this.type = type;
		this.array = array;
		this.index = 0;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return index < array.length;
	}

	@Override
	public T next() {
		try {
			current = type.getObject(array[index]);
		} catch (Exception e) {
			MLogUtil.log().d(type,array[index],e);
			index = array.length;
			throw new RuntimeException(e);
		}
		index++;
		return current;
	}

	@Override
	public void close() {
		index = array.length;
	}

	@Override
	public DbCollection<T> setRecycle(boolean on) {
		return null;
	}

	@Override
	public boolean isRecycle() {
		return false;
	}

	@Override
	public T current() throws MException {
		return current;
	}

	@Override
	public Table toTableAndClose(int maxSize) {
		return null;
	}

}
