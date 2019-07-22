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
package de.mhus.karaf.commands.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.util.DataSourceUtil;

@Command(scope = "jdbc", name = "metadata", description = "Print JDBC Metadata for a Datasource")
@Service
public class CmdJdbcMetadata extends AbstractCmd {

    @Argument(index=0, name="source", required=true, description="Datasource", multiValued=false)
    String source;

    @Argument(index=1, name="type", required=true, description=""
            + "tables <name> <types: table,view,...> <schemaPattern> <catalog>\n"
            + "indexes <table> <schema> <catalog>\n"
            + "columns <table> <column pattern> <schema pattern> <catalog>\n"
            + "catalogs\n"
            + "attributes <attribute> <type> <schema pattern> <catalog>\n"
            + "functions <function> <schema pattern> <catalog>\n"
            + "schemas\n"
            + "info", multiValued=false)
    String type;
    
    @Argument(index=2, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

    @Option(name = "-ct", aliases = { "--console-table" }, description = "Console table options", required = false, multiValued = false)
    String consoleTable;

    @Override
    public Object execute2() throws Exception {
        DataSource ds = new DataSourceUtil().getDataSource(source);
        if (ds == null) throw new MException("DataSource not found",source);
        
        if (type.equals("schemas")) {
            Connection con = ds.getConnection();
            ResultSet res = con.getMetaData().getSchemas();
            out(res);
            con.close();
        } else
        if (type.equals("attributes")) {
            Connection con = ds.getConnection();
            ResultSet res = con.getMetaData().getAttributes(attr(3,null), attr(2,null), attr(1,null), attr(0, null));
            out(res);
            con.close();
        } else
        if (type.equals("functions")) {
            Connection con = ds.getConnection();
            ResultSet res = con.getMetaData().getFunctions(attr(2,null), attr(1,null), attr(0, null));
            out(res);
            con.close();
        } else
        if (type.equals("catalogs")) {
            Connection con = ds.getConnection();
            ResultSet res = con.getMetaData().getCatalogs();
            out(res);
            con.close();
        } else
        if (type.equals("tables")) {
            Connection con = ds.getConnection();
            ResultSet res = con.getMetaData().getTables(attr(3, null), attr(2, null), attr(0, null), attrarr(1, new String[] {"TABLE"}));
            out(res);
            con.close();
        } else
        if (type.equals("indexes")) {
            Connection con = ds.getConnection();
            ResultSet res = con.getMetaData().getIndexInfo(attr(2, null), attr(1, null), attr(0, null), attrbool(3, false), attrbool(4, false));
            out(res);
            con.close();
        } else
        if (type.equals("columns")) {
            Connection con = ds.getConnection();
            ResultSet res = con.getMetaData().getColumns(attr(3, null), attr(2, null), attr(0, null), attr(1, null) );
            out(res);
            con.close();
        } else
        if (type.equals("info")) {
            Connection con = ds.getConnection();
            DatabaseMetaData m = con.getMetaData();
            System.out.println("IdentifierQuoteString: " + m.getIdentifierQuoteString());
            System.out.println("CatalogSeparator: " + m.getCatalogSeparator());
            System.out.println("CatalogTerm: " + m.getCatalogTerm());
            System.out.println("DatabaseMajorVersion: "+m.getDatabaseMajorVersion());
            System.out.println("DatabaseMinorVersion: "+m.getDatabaseMinorVersion());
            System.out.println("DatabaseProductName: "+m.getDatabaseProductName());
            System.out.println("DefaultTransactionIsolation: "+m.getDefaultTransactionIsolation());
            System.out.println("DriverMajorVersion: "+m.getDriverMajorVersion());
            System.out.println("DriverMinorVersion: "+m.getDriverMinorVersion());
            System.out.println("DriverName: "+m.getDriverName());
            System.out.println("URL: "+m.getURL());
            System.out.println("DatabaseProductVersion: "+m.getDatabaseProductVersion());
            System.out.println("UserName: "+m.getUserName());
            System.out.println("ExtraNameCharacters: "+m.getExtraNameCharacters());
            System.out.println("JDBCMajorVersion: "+m.getJDBCMajorVersion());
            System.out.println("JDBCMinorVersion: "+m.getJDBCMinorVersion());
            System.out.println("MaxBinaryLiteralLength: "+m.getMaxBinaryLiteralLength());
            System.out.println("MaxCatalogNameLength: "+m.getMaxCatalogNameLength());
            System.out.println("MaxBinaryLiteralLength: "+m.getMaxBinaryLiteralLength());
            System.out.println("MaxColumnNameLength: "+m.getMaxColumnNameLength());
            con.close();
        }
        return null;
    }

    private boolean attrbool(int index, boolean def) {
        if (parameters == null || index >= parameters.length) return def;
        return M.c(parameters[index],def);
    }

    private void out(ResultSet res) throws SQLException {
        ConsoleTable table = new ConsoleTable(consoleTable);
        int l = res.getMetaData().getColumnCount();
        String[] header = new String[l];
        for (int i = 0; i < l; i++)
            header[i] = res.getMetaData().getColumnName(i+1);
        table.setHeaderValues(header);
        Object[] row = new Object[l];
        while (res.next()) {
            for (int i = 0; i < l; i++)
                row[i] = res.getObject(i+1);
            table.addRowValues(row);
        }
        table.print();
    }

    private String[] attrarr(int index, String[] def) {
        if (parameters == null || index >= parameters.length) return def;
        if (parameters[index].equals("null")) return null;
        return parameters[index].toUpperCase().split(",");
    }
    
    private String attr(int index, String def) {
        if (parameters == null || index >= parameters.length) return def;
        if (parameters[index].equals("null")) return null;
        return parameters[index];
    }
    
}
