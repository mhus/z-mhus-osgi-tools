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
package de.mhus.osgi.jwsclient;

import java.io.IOException;

import de.mhus.osgi.jwsclient.impl.ServcieNotFoundException;

public abstract class Connection {

	protected Target target;
	
	public Target getTarget() {
		return target;
	}

	public <T> T getService(Class<? extends T> ifc) throws IOException {
		for (String name :getServiceNames()) {
			if (name.startsWith(ifc.getSimpleName()))
				return getService(name, ifc);
		}
		throw new ServcieNotFoundException(ifc.getSimpleName());
	}
	
	public abstract <T> T getService(String name, Class<? extends T> ifc) throws IOException;
	
	public abstract String[] getServiceNames();
	
}
