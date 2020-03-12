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

import de.mhus.lib.core.console.Console;
import de.mhus.lib.xdb.XdbType;
import de.mhus.osgi.api.xdb.XdbApi;
import de.mhus.osgi.api.xdb.XdbUtil;

@Command(scope = "xdb", name = "delete", description = "Delete a single object from database")
@Service
public class CmdDelete implements Action {

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

    @Option(name = "-x", description = "Output parameter", required = false)
    String outputParam = null;

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

        Object output = null;

        XdbApi api = XdbUtil.getApi(apiName);

        XdbType<?> type = api.getType(serviceName, typeName);

        if (!yes
                && Console.askQuestion(
                                "Really delete " + type + " items?",
                                new char[] {'y', 'N'},
                                true,
                                false)
                        != 'y') {
            System.out.println("Canceled by user");
            return null;
        }

        for (Object object : XdbUtil.createObjectList(type, search, null)) {
            System.out.println("*** DELETE " + object);
            type.deleteObject(object);
            output = object;
        }

        /*
        DbManagerService service = AdbUtil.getService(serviceName);
        Class<?> type = AdbUtil.getType(service, typeName);

        String regName = service.getManager().getRegistryName(type);

        for (Object object : AdbUtil.getObjects(service, type, id)) {

        	System.out.println("*** REMOVE " + object);
        	service.getManager().deleteObject(regName, object);
        	output = object;
        }
        */
        if (outputParam != null) session.put(outputParam, output);
        return null;
    }
}
