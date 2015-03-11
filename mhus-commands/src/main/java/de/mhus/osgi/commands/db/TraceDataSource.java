package de.mhus.osgi.commands.db;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.osgi.framework.BundleContext;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.logging.FileLogger;
import de.mhus.osgi.commands.impl.AbstractDataSource;
import de.mhus.osgi.commands.impl.DataSourceUtil;
import de.mhus.osgi.commands.impl.DelegateConnection;
import de.mhus.osgi.commands.impl.TracedConnection;


public class TraceDataSource extends AbstractDataSource {

	private String source;
	private BundleContext context;
	private DataSource dataSource;
	private Log log = Log.getLog(TraceDataSource.class); //TODO change !
	private boolean trace;
	private String traceFile = "";
	
	@Override
	public DataSource getDataSource() throws SQLFeatureNotSupportedException {
		
		synchronized (this) {
			if (dataSource == null) {
				dataSource = new DataSourceUtil(context).getDataSource(source);
			}
		}
		
		return dataSource;
	}

	@Override
	public void doDisconnect() {
		dataSource = null;
	}

	@Override
	public boolean isInstanceConnected() {
		return dataSource != null;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
		instanceName = "trace(" + isTrace() + "):" + source;
	}

	public BundleContext getContext() {
		return context;
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}

	public Connection getConnection() throws SQLException {
		return new TracedConnection(getDataSource().getConnection(), this);
	}

	
	public void setTrace(boolean trace) {
		this.trace = trace;
		setSource(source);
	}
	
	public void setTraceFile(String file) {
		if (MString.isEmptyTrim(file))
			log = Log.getLog(TraceDataSource.class);
		else
			log = new FileLogger("", new File(file));
		traceFile = file;
	}
	
	public boolean isTrace() {
		return log != null && trace;
	}

	public long startTrace(String ... attr) {
		if (isTrace()) {
			return System.currentTimeMillis();
		}
		return 0;
	}

	public void stopTrace(long time, String ... attr) {
		if (time == 0) return;
		long delta = System.currentTimeMillis()-time;
		log.i(source,delta,attr);
	}

	public String getTraceFile() {
		return traceFile ;
	}

}
