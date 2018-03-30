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
package de.mhus.osgi.vaadinbridge;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class VaadinConfigurableResourceProviderFinder {

	public static VaadinConfigurableResourceProviderAdmin doFind(BundleContext context) {
		ServiceReference<VaadinConfigurableResourceProviderAdmin> ref = context.getServiceReference(VaadinConfigurableResourceProviderAdmin.class);
		if (ref == null) return null;
		return context.getService(ref);
	}
	
	public static void add(final BundleContext context, final String ... pathes) {
		try {
			context.addServiceListener(new ServiceListener() {
				
				@Override
				public void serviceChanged(ServiceEvent event) {
					// only if registering
					if (event.getType() == ServiceEvent.REGISTERED) {
						// is the source bundle in the meantime removed ? quit!
						if (context.getBundle().getState() == Bundle.UNINSTALLED) {
							context.removeServiceListener(this);
							return;
						}
						// else set the configuration
						VaadinConfigurableResourceProviderAdmin service = (VaadinConfigurableResourceProviderAdmin) context.getService(event.getServiceReference());
						if (service != null)
							service.addResource(context.getBundle().getSymbolicName(), pathes);
					}
				}
			},"(objectclass="+VaadinConfigurableResourceProviderAdmin.class.getCanonicalName()+")");
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		
		VaadinConfigurableResourceProviderAdmin service = doFind(context);
		if (service != null)
			service.addResource(context.getBundle().getSymbolicName(), pathes);
	}
	
}
