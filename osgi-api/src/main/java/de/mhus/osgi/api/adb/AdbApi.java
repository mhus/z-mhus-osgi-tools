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
package de.mhus.osgi.api.adb;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;

public interface AdbApi {

    int PAGE_SIZE = 100;

    XdbService getManager();

    <T> LinkedList<T> collectResults(AQuery<T> asc, int page) throws MException;

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

    void onDelete(Persistable object);

    void collectRefereces(Persistable object, ReferenceCollector collector);

    DbSchemaService getController(String type) throws MException;
}
