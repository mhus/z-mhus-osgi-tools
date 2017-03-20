package de.mhus.osgi.jms.util;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;

public class AxisLog2LogHandler extends BasicHandler {

	private static final long serialVersionUID = 1L;

	protected static Log log = LogFactory.getLog(AxisLog2LogHandler.class.getName());

	long start = -1;
	private String logLevel;

	@Override
	public void init() {
		super.init();

		Object opt = this.getOption("LogHandler.logLevel");
		if (opt != null && opt instanceof String)
			logLevel = (String) opt;

	}

	@Override
	public void invoke(MessageContext msgContext) throws AxisFault {
		log.debug("Enter: LogHandler::invoke");
		if (msgContext.getPastPivot() == false) {
			start = System.currentTimeMillis();
		} else {
			logMessages(msgContext);
		}
		log.debug("Exit: LogHandler::invoke");
	}

	private void logMessages(MessageContext msgContext) throws AxisFault {
		try {
			
			Message inMsg = msgContext.getRequestMessage();
			Message outMsg = msgContext.getResponseMessage();
			StringBuffer msg = new StringBuffer();
			if (start != -1) {
				msg.append("= " + Messages.getMessage("elapsed00", "" + (System.currentTimeMillis() - start))).append('\n');
			}
			msg.append("= " + Messages.getMessage("inMsg00", (inMsg == null ? "null" : inMsg.getSOAPPartAsString()))).append('\n');
			msg.append("= " + Messages.getMessage("outMsg00", (outMsg == null ? "null" : outMsg.getSOAPPartAsString()))).append('\n');

			switch (logLevel) {
			case "debug": log.debug(msg); break;
			case "info": log.info(msg); break;
			case "warn": log.warn(msg); break;
			case "error": log.error(msg); break;
			default:
				log.trace(msg);
			}
			
		} catch (Exception e) {
			log.error(Messages.getMessage("exception00"), e);
			throw AxisFault.makeFault(e);
		}
	}

	@Override
	public void onFault(MessageContext msgContext) {
		try {
			logMessages(msgContext);
		} catch (AxisFault axisFault) {
			log.error(Messages.getMessage("exception00"), axisFault);
		}
	}
}
