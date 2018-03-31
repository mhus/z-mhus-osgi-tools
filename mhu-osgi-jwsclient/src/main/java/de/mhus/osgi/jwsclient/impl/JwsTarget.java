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
package de.mhus.osgi.jwsclient.impl;

import java.io.IOException;

import de.mhus.osgi.jwsclient.Client;
import de.mhus.osgi.jwsclient.Connection;
import de.mhus.osgi.jwsclient.Target;

public class JwsTarget extends Target {


	private String nameSpace;

	public JwsTarget(JwsFactory jwsFactory, Client jwsClient, String[] parts) {
		client = jwsClient;
		factory = jwsFactory;
		url = parts[2];
		
		if (parts.length > 3)
			nameSpace = parts[3];
		else {
			String wrongNs = url;
			int pos1 = url.lastIndexOf('/');
			int pos2 = url.lastIndexOf('?');
			wrongNs = url.substring(pos1+1,pos2);
			String[] wrongParts = wrongNs.split("\\.");
			String lastPart = wrongParts[wrongParts.length-1];
			StringBuilder correctNs = null;
			for (String p : wrongParts) {
				if (!p.equals(lastPart)) {
					if (correctNs == null)
						correctNs = new StringBuilder();
					else
						correctNs.insert(0, '.');
					correctNs.insert(0, p);
				}
			}			
			nameSpace = "http://" + correctNs.toString() + "/";
		}
		
		if (parts.length > 4)
			services = parts[4].split(",");
		else {
			int pos1 = url.lastIndexOf('.');
			int pos2 = url.lastIndexOf('?');
			services = new String[] { url.substring(pos1+1,pos2) + "Service" };
		}
	}

	@Override
	public Connection createConnection() throws IOException {
		return new JwsConnection(this);
	}

	@Override
	public String getUrl() {
		return url;
	}
	
	public String getNameSpace() {
		return nameSpace;
	}
		
}
