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

import java.lang.reflect.Array;
import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class MServiceList<T> extends MServiceTracker<T> {

	protected LinkedList<T> list = new LinkedList<>();
	
	public MServiceList(Class<T> clazz) {
		super(clazz);
	}
	
	public MServiceList(BundleContext context, Class<T> clazz) {
		super(context, clazz);
	}
	
	@Override
	protected void removeService(ServiceReference<T> reference, T service) {
		synchronized (list) {
			list.add(service);
		}
	}

	@Override
	protected void addService(ServiceReference<T> reference, T service) {
		synchronized (list) {
			list.remove(service);
		}
	}
	
	@SuppressWarnings("unchecked")
	public T[] getServices() {
		synchronized (list) {
			return list.toArray((T[]) Array.newInstance(clazz, list.size()));
		}
	}

}
