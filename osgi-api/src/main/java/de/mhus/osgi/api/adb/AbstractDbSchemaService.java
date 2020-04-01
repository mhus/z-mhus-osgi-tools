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

import java.util.UUID;

import de.mhus.lib.adb.Persistable;
import de.mhus.lib.basics.Ace;
import de.mhus.lib.basics.UuidIdentificable;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPeriod;
import de.mhus.lib.core.MValidator;
import de.mhus.lib.core.cfg.CfgLong;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.errors.MException;

public abstract class AbstractDbSchemaService extends MLog implements DbSchemaService {

    public static CfgLong CFG_TIMEOUT =
            new CfgLong(DbSchemaService.class, "cacheTimeout", MPeriod.MINUTE_IN_MILLISECOUNDS * 5);

    @Override
    public boolean canCreate(Persistable obj) throws MException {
        String type = obj.getClass().getCanonicalName();
        String ident = "*";
        if (obj instanceof UuidIdentificable) {
            UUID uuid = ((UuidIdentificable) obj).getId();
            if (uuid != null) ident = uuid.toString();
        }
        return AccessUtil.isPermitted(type + ":create:" + ident);
    }
    
    @Override
    public boolean canRead(Persistable obj) throws MException {
        String type = obj.getClass().getCanonicalName();
        String ident = "*";
        if (obj instanceof UuidIdentificable) {
            UUID uuid = ((UuidIdentificable) obj).getId();
            if (uuid != null) ident = uuid.toString();
        }
        return AccessUtil.isPermitted(type + ":read:" + ident);
    }

    @Override
    public boolean canUpdate(Persistable obj) throws MException {
        String type = obj.getClass().getCanonicalName();
        String ident = "*";
        if (obj instanceof UuidIdentificable) {
            UUID uuid = ((UuidIdentificable) obj).getId();
            if (uuid != null) ident = uuid.toString();
        }
        return AccessUtil.isPermitted(type + ":update:" + ident);
    }

    @Override
    public boolean canDelete(Persistable obj) throws MException {
        String type = obj.getClass().getCanonicalName();
        String ident = "*";
        if (obj instanceof UuidIdentificable) {
            UUID uuid = ((UuidIdentificable) obj).getId();
            if (uuid != null) ident = uuid.toString();
        }
        return AccessUtil.isPermitted(type + ":delete:" + ident);
    }

    @Override
    public Persistable getObject(String type, UUID id) throws MException {
        try {
            Class<?> clazz = Class.forName(type, true, this.getClass().getClassLoader());
            if (clazz != null) {
                return (Persistable) M.l(AdbApi.class).getManager().getObject(clazz, id);
            }
        } catch (Throwable t) {
            throw new MException("type error", type, t);
        }
        throw new MException("unknown type", type);
    }

    @Override
    public Persistable getObject(String type, String id) throws MException {
        try {
            if (MValidator.isUUID(id)) return getObject(type, UUID.fromString(id));
        } catch (Throwable t) {
            throw new MException("type error", type, t);
        }
        throw new MException("unknown type", type);
    }
}
