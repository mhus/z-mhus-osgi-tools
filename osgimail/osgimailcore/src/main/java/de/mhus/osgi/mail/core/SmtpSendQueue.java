package de.mhus.osgi.mail.core;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

public class SmtpSendQueue extends AbstractSendQueue {

	private static Transport transport;
	private static long lastMailTransport;
	private Session session;

	public SmtpSendQueue() {
		
	}
	
	public SmtpSendQueue(String queueName, Properties properties) {
		name = queueName;
		setProperties(properties);
	}

	@Override
	public void reset() {
		if (transport != null)
			try {
				transport.close();
			} catch (MessagingException e) {
			}
		transport = null;
		session = null;
		lastMailTransport = 0;
	}

	@Override
	public String getStatus() {
		return transport != null && transport.isConnected() ? "connected" : "disconnected";
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void sendMessage(Message message, Address[] addresses)
			throws Exception {

        lastMailTransport = System.currentTimeMillis();

        transport.sendMessage(message, addresses);

	}

	@Override
	protected void doPropertiesUpdated() {
		reset();
	}

	@Override
	public Session getSession() throws Exception {
		connect();
		return session;
	}

	protected void connect() throws MessagingException {
        if (transport != null) {
        	if (System.currentTimeMillis() - lastMailTransport > 60 * 1000 ||  !transport.isConnected()) {
        		reset();
        	}
        }

		if (session == null || transport == null) {
			
			Authenticator auth = null;
			if (properties.containsKey("mail.user")) {
		        auth = new Authenticator() {
		            public PasswordAuthentication getPasswordAuthentication() {
		                return new PasswordAuthentication(properties.getProperty("mail.user"), properties.getProperty("mail.password"));
		            }
		        };
			}
			
        	session = Session.getInstance(properties, auth);
        	transport = session.getTransport();
        	transport.connect();
        }
		
	}

}
