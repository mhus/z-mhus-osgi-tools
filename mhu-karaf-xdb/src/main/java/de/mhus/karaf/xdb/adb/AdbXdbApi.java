/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.xdb.adb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.framework.InvalidSyntaxException;

import aQute.bnd.annotation.component.Component;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.Persistable;
import de.mhus.lib.adb.model.Field;
import de.mhus.lib.adb.model.Table;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.adb.AdbUtilKaraf;
import de.mhus.lib.karaf.adb.DbManagerService;
import de.mhus.lib.xdb.XdbService;
import de.mhus.lib.xdb.XdbType;

@Component(properties="xdb.type=adb")
public class AdbXdbApi implements XdbApi {

	public static final String NAME = "adb";

	@Override
	public XdbService getService(String serviceName) throws NotFoundException {
		try {
			DbManagerService service = AdbUtilKaraf.getService(serviceName);
			
			return service.getManager();
			
		} catch (IOException | InvalidSyntaxException e) {
			throw new NotFoundException("Service not found",serviceName, e);
		}
	}

	@Override
	public <T> XdbType<T> getType(String serviceName, String typeName) throws NotFoundException {
		try {
			DbManagerService service = AdbUtilKaraf.getService(serviceName);
			
			String tableName = AdbUtilKaraf.getTableName(service, typeName);
			return service.getManager().getType(tableName);

		} catch (IOException | InvalidSyntaxException e) {
			throw new NotFoundException("Service not found",serviceName, e);
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
