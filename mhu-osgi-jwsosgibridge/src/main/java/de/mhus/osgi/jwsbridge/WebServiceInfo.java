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
package de.mhus.osgi.jwsbridge;

import javax.xml.ws.Endpoint;

public abstract class WebServiceInfo {

	private String name = "?";

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}
	
	public abstract boolean isConnected();
	
	public abstract String getStatus();

	public abstract long getId();

	public abstract String getBundleName();

	public abstract String getBindingInfo();

	public abstract Endpoint getEndpoint();
	

}
