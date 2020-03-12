/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.api.xdb;

import java.util.Iterator;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.util.Table;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbType;

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
            MLogUtil.log().d(type, array[index], e);
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
