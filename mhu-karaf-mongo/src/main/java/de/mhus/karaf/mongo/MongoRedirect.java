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
