/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.osgi.api.services.ISimpleService;
import de.mhus.osgi.api.services.SimpleService;
import de.mhus.osgi.api.util.AbstractServiceTracker;

@Component(service = ISimpleService.class, immediate = true)
public class VaultManagerImpl extends SimpleService {

    AbstractServiceTracker<KeychainSource> services;
    private MKeychain vault;

    @Activate
    public void doActivate(ComponentContext ctx) {
        services =
                new AbstractServiceTracker<KeychainSource>(ctx.getBundleContext(), KeychainSource.class) {

                    @Override
                    protected void removeService(
                            ServiceReference<KeychainSource> reference, KeychainSource service) {
                        //				MVault vault = MVaultUtil.loadDefault();
                        vault.unregisterSource(service.getName());
                    }

                    @Override
                    protected void addService(
                            ServiceReference<KeychainSource> reference, KeychainSource service) {
                        //				MVault vault = MVaultUtil.loadDefault();
                        vault.registerSource(service);
                    }
                }.start();
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        if (services != null) services.stop();
        services = null;
    }

    @Reference(service = MKeychain.class)
    public void setVault(MKeychain vault) {
        log().i("Reference Vault");
        this.vault = vault;
    }
}
