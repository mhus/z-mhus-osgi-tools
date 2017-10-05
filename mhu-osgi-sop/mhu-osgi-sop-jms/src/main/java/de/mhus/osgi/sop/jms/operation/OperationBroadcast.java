package de.mhus.osgi.sop.jms.operation;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.jms.ServerJms;
import de.mhus.lib.karaf.jms.JmsDataChannel;
import de.mhus.lib.karaf.jms.JmsDataChannelImpl;

@Component(provide=JmsDataChannel.class,immediate=true)
public class OperationBroadcast extends JmsDataChannelImpl {

	public static CfgString queueName = new CfgString(Jms2LocalOperationExecuteChannel.class, "broadcast", "mhus.operation.broadcast");
	public static CfgString connectionName = new CfgString(Jms2LocalOperationExecuteChannel.class, "connection", "sop");
	
	@Activate
	public void doActivate(ComponentContext ctx) {
		setDestination(queueName.value());
		setDestinationTopic(true);
		setChannel(null);
		setConnectionName(connectionName.value());
		setName(connectionName.value());
		reset();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		if (getServer() != null) getServer().close();
		setChannel(null);
	}

	protected ServerJms getServer() {
		return (ServerJms) getChannel();
	};

	
	@Override
	public void receivedOneWay(Message msg) throws JMSException {

	}

	@Override
	public Message received(Message msg) throws JMSException {
		TextMessage ret = getServer().getSession().createTextMessage();
		ret.setStringProperty("queue", Jms2LocalOperationExecuteChannel.instance.getQueueName());
		return ret;
	}

}
