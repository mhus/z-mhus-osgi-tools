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
package de.mhus.osgi.vaadinbridge.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.vaadinbridge.Resource;
import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;
import de.mhus.osgi.vaadinbridge.VaadinResourceProvider;

@Component(name=ConfigurableResourceProvider.NAME,servicefactory=true)
public class ConfigurableResourceProvider implements VaadinResourceProvider, VaadinConfigurableResourceProviderAdmin {

	public static Logger logger = Logger.getLogger("ConfigurableResourceProvider");
	public static final String NAME = "configurableVaadinResourceProvider";
	private static LinkedList<ResourceBundle> list = new LinkedList<ResourceBundle>();
	private BundleContext context;
	static boolean debug = false;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
// no more default since using BundleWatch
//		addResource("com.vaadin.client-compiled", "/widgetsets/com.vaadin.DefaultWidgetSet");
//		addResource("com.vaadin.server", "/vaadinBootstrap.js");
//		addResource("com.vaadin.themes", new String[] {"/themes/reindeer","/themes/runo","/themes/liferay","/themes/chameleon","/themes/base"});
//		addResource("com.vaadin.push", "/vaadinPush.js");
	}
	
	@Override
	public boolean canHandle(String name) {
		ResourceBundle resource = getResourceInfo(name);
		if (resource == null) return false;
		return true;
	}

	@Override
	public Resource getResource(String name) {
		ResourceBundle resource = getResourceInfo(name);
		if (resource == null) return null;
		Bundle bundle = resource.getBundle();
		if (bundle == null) return null;

		name = "/VAADIN" + name;
		if (debug) System.out.println("GET " + name + " FROM " + bundle.getSymbolicName());
		return new Resource( bundle, bundle.getResource(name));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public long getLastModified(String name) {
		ResourceBundle resource = getResourceInfo(name);
		if (resource == null) return -1;
		Bundle bundle = resource.getBundle();
		if (bundle == null) return -1;
		return bundle.getLastModified();
	}
	
	

	private ResourceBundle getResourceInfo(String name) {
		if (debug ) System.out.println("FIND " + name);
		synchronized (list) {
			for (ResourceBundle r : list) {
				for (String p : r.pathes) {
					if (name.startsWith(p)) {
						return r;
					}
				}
			}
		}
		if (debug ) System.out.println("NOT FOUND " + name);
		return null;
	}

	/**
	 * Add or update resource
	 * 
	 * @param bundle
	 * @param pathes
	 */
	@Override
	public void addResource(String bundle, String... pathes) {
		if (bundle == null || pathes == null)
			throw new NullPointerException();
		for (String p : pathes) if (p == null) throw new NullPointerException();
		synchronized (list) {
			for (ResourceBundle r : list) {
				if (r.bundleName.equals(bundle)) {
					r.pathes = pathes;
					return;
				}
			}
			ResourceBundle r = new ResourceBundle();
			r.bundleName = bundle;
			r.pathes = pathes;
			list.add(r);
		}
		
		cleanupCache();
		
	}

	@Override
	public void removeResource(String bundle) {
		if (bundle == null) return;
		synchronized (list) {
			for (ResourceBundle r : list) {
				if (r.bundleName.equals(bundle)) {
					list.remove(r);
					return;
				}
			}
		}
	}
	
	@Override
	public String[] getResourceBundles() {
		String[] out = new String[list.size()];
		synchronized (list) {
			int i = 0;
			synchronized (list) {
				for (ResourceBundle r : list) {
					out[i] = r.bundleName;
					i++;
				}
			}
		}
		return out;
	}
	
	@Override
	public String[] getResourcePathes(String bundle) {
		if (bundle == null) return null;
		synchronized (list) {
			for (ResourceBundle r : list) {
				if (r.bundleName.equals(bundle)) {
					return r.pathes;
				}
			}
		}
		return null;
	}

	class ResourceBundle {
		String bundleName;
		Bundle bundle = null;
		String[] pathes;
		
		public Bundle getBundle() {
			synchronized (this) {
				if (bundle == null || !(bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.INSTALLED) ) {
					for (Bundle b : context.getBundles()) {
						if (b.getSymbolicName().equals(bundleName)) {
							bundle = b;
							return bundle;
						}
					}
				}
			}
			return bundle;
		}
	}

	@Override
	public void setDebug(boolean debug) {
		ConfigurableResourceProvider.debug = debug;
	}
	
	public void cleanupCache() {
		logger.info("cleanup cache");
        synchronized (VaadinResourcesServlet.SCSS_MUTEX) {
        	for (File f : new File("vaadincache").listFiles()) {
        		if (f.isFile() && !f.getName().startsWith(".")) f.delete();
        	};
        }
	}

}
