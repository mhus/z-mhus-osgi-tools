/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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
package de.mhus.osgi.dev.dev.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import de.mhus.lib.annotations.service.ServiceActivate;
import de.mhus.lib.annotations.service.ServiceComponent;
import de.mhus.lib.annotations.service.ServiceDeactivate;
import de.mhus.lib.annotations.service.ServiceReference;
import de.mhus.lib.core.MLog;
import de.mhus.osgi.api.services.ISimpleService;

// blue-create de.mhus.osgi.dev.dev.osgi.MhusTestService
@ServiceComponent(property = "test=test")
public class MhusTestService extends MLog implements ISimpleService {

    //    @ServiceReference
    //    public BundleContext bcontext;

    @ServiceActivate
    public void doActivate() {
        log().i("doActivate");
    }

    @ServiceDeactivate
    public void doDeactivate() {
        log().i("doDeactivate");
    }

    @ServiceReference
    public void setContext(BundleContext context) {
        log().i("Set Context", context);
    }

    @ServiceReference
    public void setEventAdmin(EventAdmin eventAdmin) {
        log().i("Set Event Admin", eventAdmin);
    }

    @ServiceReference(unset = true)
    public void unsetEventAdmin(EventAdmin eventAdmin) {
        log().i("Unset Event Admin", eventAdmin);
    }

    @Override
    public String getSimpleServiceInfo() {
        return "test";
    }

    @Override
    public String getSimpleServiceStatus() {
        return "hello";
    }

    @Override
    public void doSimpleServiceCommand(String cmd, Object... param) {}
}
