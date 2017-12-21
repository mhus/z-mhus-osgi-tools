package de.mhus.karaf.mongo;

import com.mongodb.MongoClient;

import de.mhus.lib.errors.NotFoundException;

public class MongoRedirect implements MongoDataSource {

	private String target;

	public MongoRedirect(String target) {
		this.target = target;
	}
	
	protected MongoDataSource getRedirect() throws NotFoundException {
		return MongoUtil.getDatasource(target);
	}
	
	@Override
	public String getName() {
		return "Redirect:" + target;
	}

	@Override
	public int getPort() {
		MongoDataSource con = null;
		try {
			con = getRedirect();
		} catch (NotFoundException e) {
			return 0;
		}
		return con.getPort();
	}

	@Override
	public boolean isConnected() {
		MongoDataSource con = null;
		try {
			con = getRedirect();
		} catch (NotFoundException e) {
			return false;
		}
		return con.isConnected();
	}

	@Override
	public void reset() {
		MongoDataSource con = null;
		try {
			con = getRedirect();
			con.reset();
		} catch (NotFoundException e) {
		}
		
	}

	@Override
	public String getHost() {
		MongoDataSource con = null;
		try {
			con = getRedirect();
		} catch (NotFoundException e) {
			return "?";
		}
		return con.getHost();
	}

	@Override
	public MongoClient getConnection() {
		MongoDataSource con = null;
		try {
			con = getRedirect();
		} catch (NotFoundException e) {
			return null;
		}
		return con.getConnection();
	}

}
