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

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import de.mhus.lib.core.MSystem;

public abstract class AbstractTestService implements ITestService {

    protected ConfigurationAdmin configurationAdmin;

    @Activate
    public void doActivate(ComponentContext ctx) {
        System.out.println("AbstractTestService:doActivate");
    }

    @Modified
    public void doModify(ComponentContext ctx) {
        System.out.println("AbstractTestService:doModify");
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        System.out.println("AbstractTestService:doDeactivate");
    }

    @Reference
    public void setConfigurationAdmin(ConfigurationAdmin admin) {
        System.out.println("AbstractTestService=" + MSystem.getObjectId(this));
        System.out.println("AbstractTestService:setConfigurationAdmin: " + admin);
        configurationAdmin = admin;
    }
}
