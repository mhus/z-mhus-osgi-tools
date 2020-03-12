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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.xdb.XdbType;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.xdb.XdbApi;
import de.mhus.osgi.api.xdb.XdbUtil;

@Command(
        scope = "xdb",
        name = "select",
        description = "Select data from DB DataSource and print the results")
// @Parsing(XdbParser.class) see
// https://github.com/apache/karaf/tree/master/jdbc/src/main/java/org/apache/karaf/jdbc/command/parsing
@Service
public class CmdSelect extends AbstractCmd {

    @Argument(
            index = 0,
            name = "type",
            required = true,
            description = "Type to select",
            multiValued = false)
    String typeName;

    @Argument(
            index = 1,
            name = "qualification",
            required = false,
            description = "Select qualification",
            multiValued = false)
    String qualification;

    @Option(
            name = "-l",
            aliases = "--oneline",
            description = "Disable multi line output in table cells",
            required = false)
    boolean oneLine = false;

    @Option(
            name = "-m",
            aliases = "--max",
            description = "Maximum amount of chars for a value (if not full)",
            required = false)
    int max = 40;

    @Option(
            name = "-o",
            aliases = "--out",
            description = "Comma separated list of fields to print",
            required = false)
    String fieldsComma = null;

    @Option(name = "-x", description = "Output parameter", required = false)
    String outputParam = null;

    @Option(name = "-a", description = "Api Name", required = false)
    String apiName = null;

    @Option(name = "-s", description = "Service Name", required = false)
    String serviceName = null;

    @Option(name = "-v", aliases = "--csv", description = "CSV Style", required = false)
    boolean csv = false;

    @Option(
            name = "-n",
            aliases = "--lines",
            description =
                    "Number of lines f<n> (first n lines) or l<n> (last n lines) or p[<page size>,]<page>",
            required = false)
    String page = null;

    @Option(
            name = "-p",
            aliases = "--parameter",
            description = "Define a parameter key=value",
            required = false,
            multiValued = true)
    String[] parameters = null;

    @Option(name = "-q", description = "xdb query parser", required = false)
    boolean xdbQuery = false;

    @Reference private Session session;

    @Override
    public Object execute2() throws Exception {

        Object output = null;

        apiName = XdbUtil.getApiName(session, apiName);
        serviceName = XdbUtil.getServiceName(session, serviceName);

        XdbApi api = XdbUtil.getApi(apiName);
        XdbType<?> type = api.getType(serviceName, typeName);

        // sort columns to print
        final LinkedList<String> fieldNames = new LinkedList<>();
        if (fieldsComma == null) {
            for (String name : type.getAttributeNames()) {
                fieldNames.add(name);
            }

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

        } else {
            for (String name : fieldsComma.split(",")) fieldNames.add(name);
        }

        ConsoleTable out = new ConsoleTable(tblOpt);
        if (csv) {
            out.setColSeparator(";");
            out.setCellSpacer(false);
        }
        if (oneLine) out.setMultiLine(false);
        //		if (!full)
        //			out.setMaxColSize(max);
        for (String name : fieldNames) {
            if (type.isPrimaryKey(name)) name = name + "*";
            out.addHeader(name);
        }

        HashMap<String, Object> queryParam = null;
        if (parameters != null) {
            queryParam = new HashMap<>();
            for (String p : parameters) {
                String k = MString.beforeIndex(p, '=');
                String v = MString.afterIndex(p, '=');
                queryParam.put(k, v);
            }
        }

        //		if (xdbQuery) {
        //		    AQuery<?> query = Db.parse(type, qualification);
        //		}

        if (page == null) {
            for (Object object : type.getByQualification(qualification, queryParam)) {

                ConsoleTable.Row row = out.addRow();
                for (String name : fieldNames) {
                    Object value = toValue(type.get(object, name));
                    row.add(value);
                }
                output = object;
            }
        } else if (page.startsWith("f")) {
            int lines = MCast.toint(page.substring(1), 100);
            DbCollection<?> res = type.getByQualification(qualification, null);
            for (Object object : res) {
                ConsoleTable.Row row = out.addRow();
                for (String name : fieldNames) {
                    Object value = toValue(type.get(object, name));
                    row.add(value);
                }
                output = object;
                lines--;
                if (lines <= 0) {
                    res.close();
                    break;
                }
            }
        } else if (page.startsWith("l")) {
            int lines = MCast.toint(page.substring(1), 100);
            for (Object object : type.getByQualification(qualification, null)) {

                ConsoleTable.Row row = out.addRow();
                for (String name : fieldNames) {
                    Object value = toValue(type.get(object, name));
                    row.add(value);
                }
                output = object;
                if (out.size() > lines) out.removeFirstRow();
            }
        } else if (page.startsWith("p")) {
            int lines = 100;
            int p = 0;
            if (MString.isIndex(page, ',')) {
                lines = MCast.toint(MString.beforeIndex(page, ','), lines);
                p = MCast.toint(MString.afterIndex(page, ','), p);
            } else {
                p = MCast.toint(page, p);
            }
            System.out.println("Page size: " + lines + ", Page: " + p);

            DbCollection<?> res = type.getByQualification(qualification, null);
            int cnt = 0;
            Iterator<?> iter = res.iterator();
            while (iter.hasNext()) {
                iter.next();
                cnt++;
                if (cnt >= p * lines) break;
            }
            while (iter.hasNext()) {
                Object object = iter.next();
                ConsoleTable.Row row = out.addRow();
                for (String name : fieldNames) {
                    Object value = toValue(type.get(object, name));
                    row.add(value);
                }
                output = object;
                lines--;
                if (lines <= 0) {
                    res.close();
                    break;
                }
            }
        }

        out.print(System.out);

        if (outputParam != null) session.put(outputParam, output);
        return null;
    }

    private Object toValue(Object object) {
        if (object == null) return "[null]";
        return object;
    }
}
