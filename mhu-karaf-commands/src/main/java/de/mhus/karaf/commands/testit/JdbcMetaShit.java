package de.mhus.karaf.commands.testit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import de.mhus.lib.core.M;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.commands.db.DataSourceUtil;

public class JdbcMetaShit implements ShitIfc {

	@Override
	public void printUsage() {
		System.out.println("tables <ds> catalog, schemaPattern, tableNamePattern, types");
		System.out.println("indexes <ds> catalog, schema, table, unique, approximate");
	}

	@Override
	public Object doExecute(String cmd, String[] parameters) throws Exception {
		DataSource ds = new DataSourceUtil().getDataSource(parameters[0]);
		if (ds == null) throw new MException("DataSource not found",parameters[0]);
		
		if (cmd.equals("tables")) {
			Connection con = ds.getConnection();
			ResultSet res = con.getMetaData().getTables(str(parameters[1]), str(parameters[2]), str(parameters[3]), strarr(parameters[4]));
			out(res);
			con.close();
		} else
		if (cmd.equals("indexes")) {
			Connection con = ds.getConnection();
			ResultSet res = con.getMetaData().getIndexInfo(str(parameters[1]), str(parameters[2]), str(parameters[3]), bool(parameters[4]), bool(parameters[5]));
			out(res);
			con.close();
		} else
		if (cmd.equals("info")) {
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

	private boolean bool(String string) {
		return M.c(string,false);
	}

	private void out(ResultSet res) throws SQLException {
		ConsoleTable table = new ConsoleTable(false);
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

	private String[] strarr(String string) {
		if (string == null || string.equals("null")) return null;
		return string.split(",");
	}

	private String str(String string) {
		if (string ==  null || string.equals("null")) return null;
		return string;
	}

}
