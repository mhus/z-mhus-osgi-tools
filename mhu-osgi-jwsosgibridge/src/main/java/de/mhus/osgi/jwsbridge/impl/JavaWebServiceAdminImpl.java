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
package de.mhus.osgi.jwsbridge.impl;

import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.osgi.jwsbridge.JavaWebService;
import de.mhus.osgi.jwsbridge.JavaWebServiceAdmin;
import de.mhus.osgi.jwsbridge.WebServiceInfo;

@Component(name=JavaWebServiceAdmin.NAME,immediate=true)
public class JavaWebServiceAdminImpl implements JavaWebServiceAdmin {

	private LinkedList<WebServiceInfoImpl> list = new LinkedList<>();
	private BundleContext context;
	private ServiceTracker<JavaWebService, JavaWebService> tracker;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
		tracker = new ServiceTracker<JavaWebService,JavaWebService>(context, JavaWebService.class, new WSCustomizer() );
		tracker.open();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		tracker.close();
		synchronized (list) {
			for (WebServiceInfoImpl info: list)
				info.disconnect();
			list.clear();
		}
	}
	
	@Override
	public WebServiceInfo[] getWebServices() {
		synchronized (list) {
			return list.toArray(new WebServiceInfo[list.size()]);
		}
	}

	@Override
	public void closeWebService(String name) {
		synchronized (list) {
			for (WebServiceInfoImpl info : list) {
				if (info.is(name)) {
					info.disconnect();
					list.remove(info);
					return;
				}
			}
		}
	}
	
	private class WSCustomizer implements ServiceTrackerCustomizer<JavaWebService, JavaWebService> {

		@Override
		public JavaWebService addingService(
				ServiceReference<JavaWebService> reference) {

			synchronized(list) {
				WebServiceInfoImpl info = new WebServiceInfoImpl(JavaWebServiceAdminImpl.this, reference);
				if (contains(info)) {
					System.out.println("WebService already registered " + info);
					return null;
				}
				list.add(info);
				info.connect();
				return info.getJavaWebService();
			}
		}

		private boolean contains(WebServiceInfoImpl info) {
			for (WebServiceInfoImpl item : list) {
				if (item.getName().equals(info.getName())) return true;
			}
			return false;
		}

		@Override
		public void modifiedService(ServiceReference<JavaWebService> reference,
				JavaWebService service) {
			
		}

		@Override
		public void removedService(ServiceReference<JavaWebService> reference,
				JavaWebService service) {
			synchronized (list) {
				synchronized (list) {
					for (WebServiceInfoImpl info : list) {
						if (info.is(service)) {
							info.disconnect();
							list.remove(info);
							return;
						}
					}
				}
			}
		}

	}

	public BundleContext getContext() {
		return context;
	}

	@Override
	public void connect(String name) {
		synchronized (list) {
			for (WebServiceInfoImpl info : list) {
				if (info.is(name)) {
					info.connect();
					return;
				}
			}
		}
	}

	@Override
	public void disconnect(String name) {
		synchronized (list) {
			for (WebServiceInfoImpl info : list) {
				if (info.is(name)) {
					info.disconnect();
					return;
				}
			}
		}
	}

}
