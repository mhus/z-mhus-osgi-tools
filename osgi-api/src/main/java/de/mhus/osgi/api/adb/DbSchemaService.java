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

import java.util.List;
import java.util.UUID;

import de.mhus.lib.adb.Persistable;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;

public interface DbSchemaService {

    void registerObjectTypes(List<Class<? extends Persistable>> list);

    void doInitialize(XdbService dbService);

    void doDestroy();

    boolean canRead(Persistable obj) throws MException;

    boolean canUpdate(Persistable obj) throws MException;

    boolean canDelete(Persistable obj) throws MException;

    boolean canCreate(Persistable obj) throws MException;

    Persistable getObject(String type, UUID id) throws MException;

    Persistable getObject(String type, String id) throws MException;

    void collectReferences(Persistable object, ReferenceCollector collector);

    void doCleanup();

    void doPostInitialize(XdbService manager) throws Exception;

}
