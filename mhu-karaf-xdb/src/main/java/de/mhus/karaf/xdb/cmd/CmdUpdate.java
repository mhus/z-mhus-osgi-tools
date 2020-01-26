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

import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.util.Pair;
import de.mhus.lib.xdb.XdbType;
import de.mhus.osgi.api.xdb.XdbApi;
import de.mhus.osgi.api.xdb.XdbUtil;

@Command(scope = "xdb", name = "update", description = "Update a single object in database")
@Service
public class CmdUpdate implements Action {

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
            required = true,
            description = "Id of the object or query",
            multiValued = false)
    String search;

    @Argument(
            index = 2,
            name = "attributes",
            required = false,
            description = "Attributes to update, e.g user=alfons",
            multiValued = true)
    String[] attributes;

    @Option(name = "-x", description = "Output parameter", required = false)
    String outputParam = null;

    @Option(name = "-f", description = "Force Save", required = false)
    boolean force = false;

    @Option(name = "-w", description = "RAW Save", required = false)
    boolean raw = false;

    @Option(name = "-a", description = "Api Name", required = false)
    String apiName;

    @Option(name = "-s", description = "Service Name", required = false)
    String serviceName;

    @Option(name = "-y", description = "Automatic yes", required = false)
    boolean yes;

    @Reference private Session session;

    @Override
    public Object execute() throws Exception {

        apiName = XdbUtil.getApiName(session, apiName);
        serviceName = XdbUtil.getServiceName(session, serviceName);

        LinkedList<Pair<String, String>> attrObj = null;
        attrObj = new LinkedList<>();
        if (attributes != null) {
            for (String item : attributes) {
                String key = MString.beforeIndex(item, '=').trim();
                String value = MString.afterIndex(item, '=').trim();
                attrObj.add(new Pair<String, String>(key, value));
            }
        }

        Object output = null;

        XdbApi api = XdbUtil.getApi(apiName);
        XdbType<?> type = api.getType(serviceName, typeName);

        if (!yes
                && Console.askQuestion(
                                "Really update " + type + " items?",
                                new char[] {'y', 'N'},
                                true,
                                false)
                        != 'y') {
            System.out.println("Canceled by user");
            return null;
        }

        for (Object object : XdbUtil.createObjectList(type, search, null)) {
            System.out.println(">>> UPDATE " + object);

            for (Pair<String, String> entry : attrObj) {
                String name = entry.getKey();
                Object v = XdbUtil.prepareValue(type, name, entry.getValue());
                System.out.println("--- SET " + name + "  = " + v);
                try {
                    XdbUtil.setValue(type, object, name, v);
                } catch (Throwable t) {
                    System.out.println("*** Error: " + name);
                    t.printStackTrace();
                }
            }

            //			for (String name : type.getAttributeNames()) {
            //				if (attrObj.containsKey(name)) {
            //					Object v = type.prepareValue(name, attrObj.get(name));
            //					System.out.println("--- SET " + name + "  = " + v );
            //					XdbUtil.setValue(type, object, name, v);
            //				}
            //			}

            System.out.println("*** SAVE");
            if (force) type.saveObjectForce(object, raw);
            else type.saveObject(object);
            output = object;
        }

        /*
        DbManagerService service = AdbUtil.getService(serviceName);
        Class<?> type = AdbUtil.getType(service, typeName);

        HashMap<String, Object> attrObj = null;
        attrObj = new HashMap<>();
        if (attributes != null) {
        	for (String item : attributes) {
        		String key = MString.beforeIndex(item, '=').trim();
        		String value = MString.afterIndex(item, '=').trim();
        		attrObj.put(key, value);
        	}
        }

        String regName = service.getManager().getRegistryName(type);
        Table tableInfo = service.getManager().getTable(regName);

        for (Object object : AdbUtil.getObjects(service, type, id)) {

        	System.out.println(">>> UPDATE " + object);

        	for (Field f : tableInfo.getFields()) {
        		if (attrObj.containsKey(f.getName())) {
        			Object v = AdbUtil.createAttribute(f.getType(), attrObj.get(f.getName()) );
        			System.out.println("--- SET " + f.getName() + "  = " + v );
        			f.set(object, v );
        		}
        	}

        	System.out.println("*** SAVE");
        	if (force)
        		service.getManager().saveObjectForce(regName, object, raw);
        	else
        		service.getManager().saveObject(regName, object);
        	output = object;
        }
        */
        if (outputParam != null) session.put(outputParam, output);
        return null;
    }
}
