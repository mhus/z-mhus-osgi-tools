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
package de.mhus.karaf.xdb.cmd;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.xdb.XdbType;
import de.mhus.osgi.api.xdb.XdbApi;
import de.mhus.osgi.api.xdb.XdbUtil;

@Command(
        scope = "xdb",
        name = "count",
        description = "Select data from DB DataSource ant print the count of found objects")
@Service
public class CmdCount implements Action {

    @Argument(
            index = 0,
            name = "type",
            required = true,
            description = "Type to select",
            multiValued = false)
    String typeName;

    @Argument(
            index = 1,
            name = "search",
            required = false,
            description = "Select qualification",
            multiValued = false)
    String search;

    @Option(name = "-x", description = "Output parameter", required = false)
    String outputParam = null;

    @Option(name = "-a", description = "Api Name", required = false)
    String apiName;

    @Option(name = "-s", description = "Service Name", required = false)
    String serviceName;

    @Reference private Session session;

    @Override
    public Object execute() throws Exception {

        apiName = XdbUtil.getApiName(session, apiName);
        serviceName = XdbUtil.getServiceName(session, serviceName);

        XdbApi api = XdbUtil.getApi(apiName);

        XdbType<?> type = api.getType(serviceName, typeName);

        long count = type.count(search, null);
        /*
        	DbManagerService service = AdbUtil.getService(serviceName);
        	Class<?> type = AdbUtil.getType(service, typeName);

        	HashMap<String, Object> attrObj = null;
        	if (attributes != null) {
        		attrObj = new HashMap<>();
        		for (String item : attributes) {
        			String key = MString.beforeIndex(item, '=').trim();
        			String value = MString.afterIndex(item, '=').trim();
        			attrObj.put(key, value);
        		}
        	}


        	String regName = service.getManager().getRegistryName(type);
        	Table tableInfo = service.getManager().getTable(regName);

        	List<Field> pkeys = tableInfo.getPrimaryKeys();
        	final HashSet<String> pkNames = new HashSet<>();
        	for (Field f : pkeys)
        		pkNames.add(f.getName());
        	long count = service.getManager().getCountByQualification(type, qualification, attrObj);
        */

        System.out.println(count);

        if (outputParam != null) session.put(outputParam, count);
        return null;
    }
}
