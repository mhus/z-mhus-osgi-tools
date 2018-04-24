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
package de.mhus.lib.karaf.adb;

import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.jmx.MJmx;

@Component(provide=DbManagerAdmin.class,immediate=true,name="de.mhus.lib.karaf.adb.DbManagerAdmin")
public class DbManagerAdminImpl extends MJmx implements DbManagerAdmin {

	private LinkedList<DbManagerService> services = new LinkedList<>();
	
	private ServiceTracker<DbManagerService, DbManagerService> tracker;

	private BundleContext context;
	
	public DbManagerAdminImpl() {
		
	}
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		tracker = new ServiceTracker<>(context, DbManagerService.class, new MyTrackerCustomizer() );
		tracker.open();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		tracker.close();
	}
	
	@Override
	public DbManagerService[] getServices() {
		synchronized (services) {
			return services.toArray(new DbManagerService[services.size()]);
		}
	}
	
	private class MyTrackerCustomizer implements ServiceTrackerCustomizer<DbManagerService, DbManagerService> {

		@Override
		public DbManagerService addingService(
				ServiceReference<DbManagerService> reference) {

			DbManagerService service = context.getService(reference);
			try {
				addService(service);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return service;
		}

		@Override
		public void modifiedService(
				ServiceReference<DbManagerService> reference,
				DbManagerService service) {
			
		}

		@Override
		public void removedService(
				ServiceReference<DbManagerService> reference,
				DbManagerService service) {
			removeService(service);
		}
	}

	@Override
	public void addService(DbManagerService service) throws Exception {
		if (service == null) return;
		service.doInitialize();
		synchronized (services) {
			services.add(service);
		}
	}

	@Override
	public void removeService(DbManagerService service) {
		if (service == null) return;
		synchronized (services) {
			services.remove(service);
		}
		try {
			service.doClose();
		} catch (Throwable t) {
			log().e(t);
		}
	}

	@Override
	public DbManagerService getService(String name) {
		if (name == null) return null;
		synchronized (services) {
			for (DbManagerService service : services) {
				if (name.equals(service.getServiceName()))
					return service;
			}
		}
		return null;
	}
}
