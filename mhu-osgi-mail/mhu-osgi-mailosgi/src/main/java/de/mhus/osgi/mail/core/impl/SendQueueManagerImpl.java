package de.mhus.osgi.mail.core.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;

import aQute.bnd.annotation.component.Component;
import de.mhus.osgi.mail.core.QueueNotFoundException;
import de.mhus.osgi.mail.core.SendQueue;
import de.mhus.osgi.mail.core.SendQueueManager;
import de.mhus.osgi.mail.core.SmtpSendQueue;

@Component(name="DefaultSendQueueManager",immediate=true)
public class SendQueueManagerImpl implements SendQueueManager {

	private static Logger log = Logger.getLogger(SendQueueManagerImpl.class.getCanonicalName());
	private HashMap<String, SendQueue> queues = new HashMap<>();
	
	@Override
	public void sendMail(Message message, Address[] addresses) throws Exception {
		sendMail(QUEUE_DEFAULT, message, addresses);
	}
	
	@Override
	public void sendMail(String queue, Message message, Address[] addresses) throws Exception {
		SendQueue q = getQueue(queue);
		if (q == null) throw new QueueNotFoundException(queue);
		q.sendMessage(message, addresses);
	}

	@Override
	public SendQueue getQueue(String name) {
		synchronized (queues) {
			SendQueue ret = queues.get(name);
			if (ret == null) {
				ret = loadQueue(name);
			}
			return ret;
		}
	}

	private SendQueue loadQueue(String name) {
		Properties p = new Properties();
		File file = new File("etc/smtp-" + name + ".properties");
		if (!file.exists()) {
			log.warning("SMTP Config Not Found: " + file.getAbsolutePath());
			return null;
		}
		try {
			FileInputStream is = new FileInputStream(file);
			p.load(is);
			is.close();
		} catch (Throwable t) {
			log.fine(t.toString());
		}
		registerQueue(new SmtpSendQueue(name, p));
		
		return queues.get(name);
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
	public void sendMail(Message message, Address addresse)
			throws Exception {
		sendMail(QUEUE_DEFAULT, message, new Address[] {addresse});
	}
	
	@Override
	public void sendMail(String queue, Message message, Address addresse)
			throws Exception {
		sendMail(queue, message, new Address[] {addresse});
	}

}
