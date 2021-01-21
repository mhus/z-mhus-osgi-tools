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
package de.mhus.osgi.services.deploy;

import java.io.File;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.MOsgi.Service;
import de.mhus.osgi.api.deploy.DeployService;
import de.mhus.osgi.api.services.ISimpleService;
import de.mhus.osgi.api.util.MServiceTracker;
import de.mhus.osgi.services.deploy.BundleDeployer.SENSIVITY;

@Component(immediate = true)
public class DeployServiceManager extends MLog implements ISimpleService {

    MServiceTracker<DeployService> tracker =
            new MServiceTracker<DeployService>(DeployService.class) {

                @Override
                protected void removeService(
                        ServiceReference<DeployService> reference, DeployService service) {
                    undeploy(reference, service);
                }

                @Override
                protected void addService(
                        ServiceReference<DeployService> reference, DeployService service) {
                    deploy(reference, service, SENSIVITY.UPDATE);
                }
            };

    @Activate
    public void doActivate(ComponentContext ctx) {
        tracker.start(ctx);
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        tracker.stop();
    }

    @Override
    public String getSimpleServiceInfo() {
        return "redeploy <bundle>";
    }

    protected void deploy(
            ServiceReference<DeployService> reference, DeployService service, SENSIVITY sensivity) {
        for (String path : service.getResourcePathes()) {
            log().i("deploy", reference.getBundle().getSymbolicName(), path, sensivity);
            try {
                File dir = BundleDeployer.deploy(reference.getBundle(), path, sensivity);
                service.setDeployDirectory(path, dir);
            } catch (Throwable e) {
                log().w(reference, path, e);
            }
        }
    }

    protected void undeploy(ServiceReference<DeployService> reference, DeployService service) {
        for (String path : service.getResourcePathes()) {
            log().i("undeploy", reference.getBundle().getSymbolicName(), path);
            try {
                service.setDeployDirectory(path, null);
                BundleDeployer.delete(reference.getBundle(), path);
            } catch (Throwable e) {
                log().w(reference, path, e);
            }
        }
    }

    @Override
    public String getSimpleServiceStatus() {
        return "";
    }

    @Override
    public void doSimpleServiceCommand(String cmd, Object... param) {
        try {
            if (cmd.equals("redeploy")) {
                String bundleName = String.valueOf(param[0]);
                for (Service<DeployService> ref : MOsgi.getServiceRefs(DeployService.class, null)) {
                    if (ref.getReference().getBundle().getSymbolicName().equals(bundleName)) {
                        System.out.println("Redeploy " + ref);
                        undeploy(ref.getReference(), ref.getService());
                        deploy(ref.getReference(), ref.getService(), SENSIVITY.CLEANUP);
                    }
                }
            } else if (cmd.equals("list")) {
                ConsoleTable table = new ConsoleTable();
                table.setHeaderValues("Bundle", "Name", "Deploy Dir");
                for (Service<DeployService> ref : MOsgi.getServiceRefs(DeployService.class, null)) {
                    table.addRowValues(
                            ref.getReference().getBundle().getSymbolicName(),
                            ref.getService().getClass().getCanonicalName(),
                            ref.getService().getDeployDirectory());
                }
                table.print(System.out);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
