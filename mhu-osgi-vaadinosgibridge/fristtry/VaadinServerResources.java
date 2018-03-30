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

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.vaadinbridge.VaadinResourceProvider;

@Component(name=VaadinServerResources.NAME,servicefactory=true)
public class VaadinServerResources implements VaadinResourceProvider {

	public static final String NAME = "com.vaadin.server";
	private Bundle targetBundle;
	private BundleContext context;
	
	@Override
	public boolean canHandle(String name) {
		if (name.equals("/vaadinBootstrap.js"))
			return true;
		return false;
	}

	@Override
	public URL getResource(String name) {
		
		if (name.equals("/vaadinBootstrap.js")) name = "/VAADIN" + name;
		
		Bundle bundle = getTargetBundle();
		if (bundle == null) {
			System.out.println("Bundle not found " + NAME);
			return null;
		}
		return bundle.getResource(name);
	}
	
	@Activate
	public void activate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
	}

	private Bundle getTargetBundle() {
		if (targetBundle != null && targetBundle.getState() != Bundle.UNINSTALLED) {
            return targetBundle;
        }
		for (Bundle b : context.getBundles()) {
			System.out.println("NAME: " + b.getSymbolicName());
			if (b.getSymbolicName().equals(NAME)) {
				targetBundle = b;
				break;
			}
		}
		
		return targetBundle;
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public long getLastModified(String name) {
		Bundle bundle = getTargetBundle();
		if (bundle == null) return -1;
		return bundle.getLastModified();
	}

}
