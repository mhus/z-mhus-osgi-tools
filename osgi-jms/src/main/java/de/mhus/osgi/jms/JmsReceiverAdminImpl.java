/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.jms;

import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.osgi.api.jms.JmsReceiver;
import de.mhus.osgi.api.jms.JmsReceiverAdmin;

@Component(name = "JmsReceiverAdmin", immediate = true)
public class JmsReceiverAdminImpl implements JmsReceiverAdmin {

    private LinkedList<JmsReceiver> receivers = new LinkedList<JmsReceiver>();

    @Activate
    public void doActivate(ComponentContext ctx) {}

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        for (JmsReceiver r : list()) remove(r.getName());
    }

    @Override
    public void add(JmsReceiver receiver) {
        receiver.init(this);
        receivers.add(receiver);
    }

    @Override
    public void remove(String queue) {
        for (JmsReceiver r : receivers) {
            if (r.getName().equals(queue)) {
                receivers.remove(r);
                r.close();
                return;
            }
        }
    }

    @Override
    public JmsReceiver[] list() {
        return receivers.toArray(new JmsReceiver[receivers.size()]);
    }

    public static JmsReceiverAdmin findAdmin() {
        BundleContext bc = FrameworkUtil.getBundle(JmsReceiverAdminImpl.class).getBundleContext();
        ServiceReference<JmsReceiverAdmin> ref = bc.getServiceReference(JmsReceiverAdmin.class);
        return bc.getService(ref);
    }
}
