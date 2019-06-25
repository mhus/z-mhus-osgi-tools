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
package de.mhus.osgi.services;

import org.osgi.service.component.annotations.Component;

import de.mhus.lib.annotations.jmx.JmxManaged;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.jmx.JmxObjectMBean;
import de.mhus.lib.core.jmx.MJmx;
import de.mhus.osgi.api.services.CacheControlIfc;
import de.mhus.osgi.api.services.MOsgi;

// http://localhost:8181/jolokia/read/de.mhus.lib.core.jmx.JmxObject:name=de.mhus.lib.karaf.services.JmxCacheControl,type=JmxCacheControl
@Component(immediate=true,service=JmxObjectMBean.class)
@JmxManaged(descrition = "Cache Control Service")
public class JmxCacheControl extends MJmx {

	@JmxManaged
	public String[][] getTable() {
		ConsoleTable table = new ConsoleTable();
		table.setHeaderValues("Name","Size","Enabled","Status");
		for (CacheControlIfc c : MOsgi.getServices(CacheControlIfc.class, null))
			try {
				table.addRowValues(c.getName(), c.getSize(),c.isEnabled(), "ok");
			} catch (Throwable t) {
				log().d(c,t);
				table.addRowValues(c.getName(), "","", t.getMessage());
			}
		return table.toStringMatrix(false);
	}

}
