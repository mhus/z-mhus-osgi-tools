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
package de.mhus.lib.karaf;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.base.service.TimerImpl;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.util.Version;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotFoundRuntimeException;

public class MOsgi {
	
	public static final String COMPONENT_NAME = "component.name";

	private static final Log log = Log.getLog(MOsgi.class);
	private static Timer localTimer; // fallback timer

	public static <T> T getService(Class<T> ifc) throws NotFoundException {
		BundleContext context = FrameworkUtil.getBundle(ifc).getBundleContext();
		if (context == null)
			context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
		if (context == null)
			throw new NotFoundException("service context not found", ifc);
		ServiceReference<T> ref = context.getServiceReference(ifc);
		if (ref == null) throw new NotFoundException("service reference not found", ifc);
		T obj = context.getService(ref);
		if (obj == null) throw new NotFoundException("service not found", ifc);
		return obj;
	}

	public static <T> T getService(Class<T> ifc, String filter) throws NotFoundException {
		List<T> list = getServices(ifc, filter);
		if (list.size() == 0) throw new NotFoundException("service not found", ifc, filter);
		return list.get(0);
	}

	public static <T> List<T> getServices(Class<T> ifc, String filter) {
		BundleContext context = FrameworkUtil.getBundle(ifc).getBundleContext();
		if (context == null)
			context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
		if (context == null)
			throw new NotFoundRuntimeException("service context not found", ifc);
		LinkedList<T> out = new LinkedList<>();
		try {
			for (ServiceReference<T> ref : context.getServiceReferences(ifc, filter)) {
				T obj = context.getService(ref);
				out.add(obj);
			}
		} catch (Exception e) {
			log.d(ifc,filter,e);
		}
		return out;
	}

	public static <T> List<Service<T>> getServiceRefs(Class<T> ifc, String filter) {
		BundleContext context = FrameworkUtil.getBundle(ifc).getBundleContext();
		if (context == null)
			context = FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
		if (context == null)
			throw new NotFoundRuntimeException("service context not found", ifc);
		LinkedList<Service<T>> out = new LinkedList<>();
		try {
			for (ServiceReference<T> ref : context.getServiceReferences(ifc, filter)) {
				out.add(new Service<T>(ref,context));
			}
		} catch (Exception e) {
			log.d(ifc,filter,e);
		}
		return out;
	}

	public static synchronized TimerIfc getTimer() {
		TimerIfc timer = null;
		try {
			timer = getService(TimerFactory.class).getTimer();
		} catch (Throwable t) {}
		if (timer == null) {
			// oh oh
			if (localTimer == null)
				localTimer = new Timer("de.mhu.lib.localtimer",true);
			timer = new TimerImpl( localTimer );
		}
		return timer;
	}
	
	public static String filterServiceId(String name) {
		return "("+Constants.SERVICE_ID+"=" + name + ")";
	}
	
	public static String filterServiceName(String name) {
		return "("+COMPONENT_NAME+"=" + name + ")";
	}
	
	public static String filterObjectClass(String clazz) {
		return "("+Constants.OBJECTCLASS+"=" + clazz + ")";
	}

	public static String filterAdd(String ... parts) {
		StringBuilder out = new StringBuilder().append("(&");
		for (String part : parts)
			out.append(part);
		out.append(")");
		return out.toString();
	}
	
	public static String getServiceId(ServiceReference<?> ref) {
		if (ref == null) return null;
		return String.valueOf( ref.getProperty(Constants.SERVICE_ID));
	}
	
	public static String getServiceName(ServiceReference<?> ref) {
		if (ref == null) return null;
		return String.valueOf( ref.getProperty(COMPONENT_NAME));
	}

	public static class Service<T> {

		private ServiceReference<T> ref;
		private T obj;
		private BundleContext context;

		public Service(ServiceReference<T> ref, BundleContext context) {
			this.ref = ref;
			this.obj = null;
			this.context = context;
		}
		
		public T getService() {
			if (obj == null)
				obj = context.getService(ref);
			return obj;
		}
		
		public ServiceReference<T> getReference() {
			return ref;
		}
		
		public String getName() {
			return MOsgi.getServiceName(ref);
		}
	}

	/**
	 * This function returns in every case a valid bundle context. It's the context of a base
	 * bundle, not the context of the current working bundle. Use the context to access services
	 * in every case.
	 * 
	 * @return BundleContext
	 */
	public static BundleContext getBundleContext() {
		return FrameworkUtil.getBundle(MOsgi.class).getBundleContext();
	}

	public enum BUNDLE_STATE {UNINSTALLED,INSTALLED,RESOLVED,STARTING,STOPPING,ACTIVE,UNKNOWN}
	public static BUNDLE_STATE getState(Bundle bundle) {
		int state = bundle.getState();
		switch (state) {
		case Bundle.UNINSTALLED: return BUNDLE_STATE.UNINSTALLED;
		case Bundle.INSTALLED: return BUNDLE_STATE.INSTALLED;
		case Bundle.RESOLVED: return BUNDLE_STATE.RESOLVED;
		case Bundle.STARTING: return BUNDLE_STATE.STARTING;
		case Bundle.STOPPING: return BUNDLE_STATE.STOPPING;
		case Bundle.ACTIVE: return BUNDLE_STATE.ACTIVE;
		default: return BUNDLE_STATE.UNKNOWN;
		}
	}

	public static Version getBundelVersion(Class<?> owner) {
		Bundle bundle = FrameworkUtil.getBundle(owner);
		if (bundle == null) return Version.V_0_0_0;
		return new Version(bundle.getVersion().toString());
	}

	public static File getTmpFolder() {
		File dir = new File("data/tmp");
		if (dir.exists()) return dir;
		return new File(MSystem.getTmpDirectory());
	}

	/**
	 * Return the bundle with the given name or throw NotFoundException
	 * @param name
	 * @return The Bundle
	 * @throws NotFoundException 
	 */
	public static Bundle getBundle(String name) throws NotFoundException {
		for (Bundle bundle : FrameworkUtil.getBundle(MOsgi.class).getBundleContext().getBundles())
			if (bundle.getSymbolicName().equals(name)) return bundle;
		throw new NotFoundException("Bundle not found",name);
	}
	
}
