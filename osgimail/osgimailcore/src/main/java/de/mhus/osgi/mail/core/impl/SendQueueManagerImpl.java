package de.mhus.osgi.mail.core.impl;

import java.util.HashMap;

import javax.mail.Address;
import javax.mail.Message;

import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.mail.core.QueueNotFoundException;
import de.mhus.osgi.mail.core.SendQueueManager;
import de.mhus.osgi.mail.core.SendQueue;

@Component(name="DefaultSendQueueManager",immediate=true)
public class SendQueueManagerImpl implements SendQueueManager {

	private HashMap<String, SendQueue> queues = new HashMap<>();
	
	@Override
	public void sendMail(String queue, Message message, Address[] addresses) throws Exception {
		SendQueue q = getQueue(queue);
		if (q == null) throw new QueueNotFoundException(queue);
		q.sendMessage(message, addresses);
	}

	@Override
	public SendQueue getQueue(String name) {
		synchronized (queues) {
			return queues.get(name);
		}
	}

	@Override
	public void registerQueue(SendQueue queue) {
		synchronized (queues) {
			queues.put(queue.getName(), queue);
		}
	}

	@Override
	public void unregisterQueue(String name) {
		synchronized (queues) {
			queues.remove(name);
		}
	}

	@Override
	public String[] getQueueNames() {
		synchronized (queues) {
			return queues.keySet().toArray(new String[queues.size()]);
		}
	}

	@Override
	public void sendMail(String queue, Message message, Address addresse)
			throws Exception {
		sendMail(queue, message, new Address[] {addresse});
	}

}
