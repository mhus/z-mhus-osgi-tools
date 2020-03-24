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

import java.util.HashMap;

import de.mhus.lib.adb.DbSchema;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.adb.transaction.MemoryLockStrategy;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.sql.DbConnection;

public abstract class AbstractDbSchema extends DbSchema {

//    private Log trace =
//            new SopFileLogger(
//                    MApi.getCfg(DbManagerService.class).getExtracted("traceLoggerName", "db"),
//                    getClass().getCanonicalName());

    public AbstractDbSchema() {
//        trace.i("start");
        lockStrategy = new MemoryLockStrategy();
        ((MemoryLockStrategy) lockStrategy)
                .setMaxLockAge(
                        MApi.getCfg(DbManagerService.class)
                                .getLong("maxLockAge", MPeriod.MINUTE_IN_MILLISECOUNDS * 5));
    }

    @Override
    public void authorizeSaveForceAllowed(DbConnection con, Table table, Object object, boolean raw)
            throws AccessDeniedException {
        if (!AccessUtil.isAdmin())
            throw new AccessDeniedException();
    }

    @Override
    public void authorizeUpdateAttributes(
            DbConnection con, Table table, Object object, boolean raw, String... attributeNames)
            throws AccessDeniedException {
        if (!AccessUtil.isAdmin())
            throw new AccessDeniedException();
    }

    @Override
    public void internalCreateObject(
            DbConnection con, String name, Object object, HashMap<String, Object> attributes) {
        super.internalCreateObject(con, name, object, attributes);
//        trace.i("create", name, attributes, object);
    }

    @Override
    public void internalSaveObject(
            DbConnection con, String name, Object object, HashMap<String, Object> attributes) {
        super.internalSaveObject(con, name, object, attributes);
//        trace.i("modify", name, attributes, object);
    }

    @Override
    public void internalDeleteObject(
            DbConnection con, String name, Object object, HashMap<String, Object> attributes) {
        super.internalDeleteObject(con, name, object, attributes);
//        trace.i("delete", name, attributes, object);
    }
}
