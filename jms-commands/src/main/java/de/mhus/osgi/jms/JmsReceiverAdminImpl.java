package de.mhus.osgi.jms;

import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component(name="JmsReceiverAdmin",immediate=true)
public class JmsReceiverAdminImpl implements JmsReceiverAdmin {

	private LinkedList<JmsReceiver> receivers = new LinkedList<JmsReceiver>();
	
	@Activate
	public void doActivate(ComponentContext ctx) {
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		for (JmsReceiver r : list())
			remove(r.getName());
	}
	
	public void add(JmsReceiver receiver) {
		receiver.init(this);
		receivers.add(receiver);
	}

	public void remove(String queue) {
		for (JmsReceiver r : receivers) {
			if (r.getName().equals(queue)) {
				receivers.remove(r);
				r.close();
				return;
			}
		}
	}

	public JmsReceiver[] list() {
		return receivers.toArray(new JmsReceiver[receivers.size()]);
	}
	
	
	public static JmsReceiverAdmin findAdmin() {
		BundleContext bc = FrameworkUtil.getBundle(JmsReceiverAdminImpl.class).getBundleContext();
		ServiceReference<JmsReceiverAdmin> ref = bc.getServiceReference(JmsReceiverAdmin.class);
		return bc.getService(ref);
	}
}
