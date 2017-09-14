package de.mhus.osgi.sop.api.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.jms.JmsInterceptor;
import de.mhus.osgi.sop.api.aaa.AccessApi;

public class TicketAccessInterceptor extends MLog implements JmsInterceptor {

	public static final String TICKET_KEY = "mhus.ticket"; // TODO configurable

	@Override
	public void begin(Message message) {
		String ticket;
		try {
			ticket = message.getStringProperty(TICKET_KEY);
		} catch (JMSException e) {
			throw new MRuntimeException(e);
		}
		try {
			if (ticket == null) throw new AccessDeniedException("call service without ticket");
			MApi.lookup(AccessApi.class).process(ticket);
		} catch (Throwable t) {
			log().i("Incoming Access Denied",message);
			throw t;
		}
	}

	@Override
	public void end(Message message) {
		String ticket;
		try {
			ticket = message.getStringProperty(TICKET_KEY);
		} catch (JMSException e) {
			throw new MRuntimeException(e);
		}
		if (ticket == null) throw new AccessDeniedException("call service without ticket");
		MApi.lookup(AccessApi.class).release(ticket);
	}

	@Override
	public void prepare(Message answer) {

		
	}

	@Override
	public void answer(Message message) {
		
	}

}
