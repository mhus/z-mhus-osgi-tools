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

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.VaultSource;
import de.mhus.osgi.services.util.MServiceTracker;

@Component(service=SimpleServiceIfc.class,immediate=true)
public class VaultManagerImpl extends SimpleService {
	
	MServiceTracker<VaultSource> services;
	private MVault vault;
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		services = new MServiceTracker<VaultSource>(ctx.getBundleContext(),VaultSource.class) {
			
			@Override
			protected void removeService(ServiceReference<VaultSource> reference, VaultSource service) {
//				MVault vault = MVaultUtil.loadDefault();
				vault.unregisterSource(service.getName());
			}
			
			@Override
			protected void addService(ServiceReference<VaultSource> reference, VaultSource service) {
//				MVault vault = MVaultUtil.loadDefault();
				vault.registerSource(service);
			}
		}.start();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		if (services != null)
			services.stop();
		services = null;
	}

	@Reference(service=MVault.class)
	public void setVault(MVault vault) {
		log().i("Reference Vault");
		this.vault = vault;
	}
	

}
