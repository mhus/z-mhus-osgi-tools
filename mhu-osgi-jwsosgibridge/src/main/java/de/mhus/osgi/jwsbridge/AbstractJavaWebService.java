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

public abstract class AbstractJavaWebService implements JavaWebService {

	@Override
	public String getServiceName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void published(WebServiceInfo info) {
		
	}

	@Override
	public void stopped(WebServiceInfo info) {
		
	}
	
	@Override
	public Object getServiceObject() {
		return this;
	}

}
