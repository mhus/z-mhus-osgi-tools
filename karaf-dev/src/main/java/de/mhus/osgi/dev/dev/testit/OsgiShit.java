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
package de.mhus.osgi.dev.dev.testit;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.mhus.lib.core.MSystem;
import de.mhus.osgi.dev.dev.CmdDev;

public class OsgiShit implements ShitIfc {

    private static ServiceRegistration<EventHandler> registrationEventHandler;
    public static String[] blacklist =
            new String[] {
                "org/osgi/service/log/LogEntry" // ignore ... too much useless events
            };

    @Override
    public void printUsage() {
        System.out.println("sessionid                      - print current session id");
        System.out.println(
                "registerEventHandler <topic>*  - register/unregister event handler, use *, e.g. com/acme/reportgenerator/*");
        System.out.println(
                "blacklist [starts with]*       - set event handler blacklist, e.g. org/osgi/service/log/LogEntry");
        System.out.println("services <ifc>");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {

        if (cmd.equals("blacklist")) {
            blacklist = parameters;
        } else if (cmd.equals("registerEventHandler")) {
            if (registrationEventHandler == null) {
                @SuppressWarnings("rawtypes")
                Dictionary props = new Hashtable();
                props.put(EventConstants.EVENT_TOPIC, parameters);
                BundleContext bundleContext =
                        FrameworkUtil.getBundle(CmdDev.class).getBundleContext();
                System.out.println("Register");
                registrationEventHandler =
                        bundleContext.registerService(
                                EventHandler.class.getName(), new PrintEventHandler(), props);
            } else {
                System.out.println("Unregister");
                registrationEventHandler.unregister();
                registrationEventHandler = null;
            }
        } else if (cmd.equals("sessionid")) {
            System.out.println(MSystem.getObjectId(base.getSession()));
        } else if (cmd.equals("services")) {
            BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
            ServiceReference<?>[] refs =
                    context.getServiceReferences(parameters[0], null);
            if (refs != null)
                for (ServiceReference<?> ref : refs)
                    System.out.println(ref.getBundle().getSymbolicName() + " " + ref.getBundle().getBundleId() + " " + context.getService(ref).getClass().getCanonicalName());
            else
                System.out.println("Not found");
        }

        return null;
    }
}
