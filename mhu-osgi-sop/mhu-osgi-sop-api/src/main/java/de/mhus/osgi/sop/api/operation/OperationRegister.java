package de.mhus.osgi.sop.api.operation;

import java.util.Date;

import de.mhus.lib.core.util.Version;

/**
 * The class represent an address to an operation via jms like a uri
 * 
 * @author mikehummel
 *
 */
public class OperationRegister {
	
	private String connection;
	private String queue;
	private String path;
	private Version version;
	private long lastUpdated;
	
	public OperationRegister(String connection, String queue, String path, String version) {
		super();
		this.connection = connection;
		this.queue = queue;
		this.path = path;
		this.version = new Version(version);
	}
	
	public String getConnection() {
		return connection;
	}
	public String getQueue() {
		return queue;
	}
	public String getPath() {
		return path;
	}
	public Version getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return "jms://" + (connection == null || connection.length() == 0 ? "" : connection + ":") + queue + "/" + path + (version == null? "" : "/" + version);
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated() {
		this.lastUpdated = System.currentTimeMillis();
	}
}
