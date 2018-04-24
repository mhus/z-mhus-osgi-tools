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
package de.mhus.lib.karaf.jms;

import javax.jms.JMSException;

import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.jms.JmsConnection;

public class JmsDataSourceOpenWire implements JmsDataSource {

	private String name;
	private String url;
	private String user;
	private String password;
	
	private JmsConnection con = null;
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public JmsConnection createConnection() throws JMSException {
		return new JmsConnection(url, user, password);
	}

	@Override
	public JmsConnection getConnection() throws JMSException {
		synchronized (this) {
			if (con == null) con = createConnection();
			return con;
		}
	}

	@Override
	public void resetConnection() throws JMSException {
		synchronized (this) {
			if (con != null) 
			try {
				con.close();
			} catch (Throwable t) {
				MLogUtil.log().d(name,t);
			}
			con = null;
		}
	}

}
