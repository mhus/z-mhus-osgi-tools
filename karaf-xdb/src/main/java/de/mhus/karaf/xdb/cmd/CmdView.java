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

import java.util.Comparator;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.xdb.XdbType;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.xdb.XdbApi;
import de.mhus.osgi.api.xdb.XdbUtil;

@Command(scope = "xdb", name = "view", description = "Show a object")
@Service
public class CmdView extends AbstractCmd {

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
            description = "Id of the object or query in brakets e.g '($db.table.field$ = 1)'",
            multiValued = false)
    String search;

    @Option(
            name = "-o",
            aliases = "--out",
            description = "Comma separated list of fields to print",
            required = false)
    String fieldsComma = null;

    @Option(
            name = "-v",
            aliases = "--verbose",
            description = "Try to analyse Objects and print the values separately",
            required = false)
    boolean verbose = false;

    @Option(
            name = "-m",
            aliases = "--max",
            description = "Maximum amount of chars for a value (if not full)",
            required = false)
    int max = 40;

    @Option(name = "-x", description = "Output parameter", required = false)
    String outputParam = null;

    @Option(name = "-a", description = "Api Name", required = false)
    String apiName;

    @Option(name = "-s", description = "Service Name", required = false)
    String serviceName;

    @Reference private Session session;

    @Override
    public Object execute2() throws Exception {

        apiName = XdbUtil.getApiName(session, apiName);
        serviceName = XdbUtil.getServiceName(session, serviceName);

        Object output = null;

        XdbApi api = XdbUtil.getApi(apiName);
        XdbType<?> type = api.getType(serviceName, typeName);

        for (Object object : XdbUtil.createObjectList(type, search, null)) {

            if (object == null) {
                System.out.println("*** Object not found");
                continue;
            }
            System.out.println(">>> VIEW " + type.getIdAsString(object));

            ConsoleTable out = new ConsoleTable(tblOpt);
            out.setHeaderValues("Field", "Value", "Type");

            List<String> fieldNames = type.getAttributeNames();
            fieldNames.sort(
                    new Comparator<String>() {

                        @Override
                        public int compare(String o1, String o2) {
                            boolean pk1 = type.isPrimaryKey(o1);
                            boolean pk2 = type.isPrimaryKey(o2);
                            if (pk1 == pk2) return o1.compareTo(o2);
                            if (pk1) return -1;
                            // if (pk2) return 1;
                            return 1;
                        }
                    });

            for (String name : fieldNames) {
                Object v = type.get(object, name);
                out.addRowValues(name, v, type.getAttributeType(name));
            }

            out.print();
            output = object;
        }

        if (outputParam != null) session.put(outputParam, output);
        return null;
    }
}
