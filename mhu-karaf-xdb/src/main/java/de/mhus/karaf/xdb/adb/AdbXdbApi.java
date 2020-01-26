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
package de.mhus.karaf.xdb.adb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.lib.xdb.XdbType;
import de.mhus.osgi.api.adb.AdbUtilKaraf;
import de.mhus.osgi.api.adb.DbManagerService;
import de.mhus.osgi.api.xdb.XdbApi;

@Component(property = "xdb.type=adb")
public class AdbXdbApi implements XdbApi {

    public static final String NAME = "adb";

    @Override
    public XdbService getService(String serviceName) throws NotFoundException {
        try {
            DbManagerService service = AdbUtilKaraf.getService(serviceName);

            return service.getManager();

        } catch (IOException | InvalidSyntaxException e) {
            throw new NotFoundException("Service not found", serviceName, e);
        }
    }

    @Override
    public <T> XdbType<T> getType(String serviceName, String typeName) throws NotFoundException {
        try {
            DbManagerService service = AdbUtilKaraf.getService(serviceName);

            String tableName = AdbUtilKaraf.getTableName(service, typeName);
            return service.getManager().getType(tableName);

        } catch (IOException | InvalidSyntaxException e) {
            throw new NotFoundException("Service not found", serviceName, e);
        }
    }

    @Override
    public List<String> getServiceNames() {
        LinkedList<String> out = new LinkedList<>();
        for (DbManagerService s : AdbUtilKaraf.getServices(false)) {
            out.add(s.getServiceName());
        }
        return out;
    }
}
