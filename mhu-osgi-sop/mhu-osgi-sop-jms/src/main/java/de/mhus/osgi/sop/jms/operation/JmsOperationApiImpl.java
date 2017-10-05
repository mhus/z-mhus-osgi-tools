package de.mhus.osgi.sop.jms.operation;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimerTask;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.osgi.service.component.ComponentContext;

import com.vaadin.data.util.GeneratedPropertyContainer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MThread;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.base.service.TimerFactory;
import de.mhus.lib.core.base.service.TimerIfc;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VersionRange;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.MJms;
import de.mhus.lib.jms.ServerJms;
import de.mhus.lib.karaf.jms.JmsUtil;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.operation.JmsOperationApi;
import de.mhus.osgi.sop.api.operation.LocalOperationApi;
import de.mhus.osgi.sop.api.operation.OperationRegister;

@Component(immediate=true)
public class JmsOperationApiImpl extends MLog implements JmsOperationApi {

	private ClientJms registerClient;
	private ServerJms registerServer;
	private HashMap<String, OperationRegister> register = new HashMap<>();
	private TimerIfc timer;
	protected long lastRegistryRequest;

	@Activate
	public void doActivate(ComponentContext ctx) {
		registerClient = new ClientJms(new JmsDestination(JmsOperationApi.REGISTRY_TOPIC, true));
		registerServer = new ServerJms(new JmsDestination(JmsOperationApi.REGISTRY_TOPIC, true)) {
			
			@Override
			public void receivedOneWay(Message msg) throws JMSException {
				if (msg instanceof MapMessage && 
					!Jms2LocalOperationExecuteChannel.queueName.value().equals(msg.getStringProperty("queue")) // do not process my own messages
				   ) {
					
					MapMessage m = (MapMessage)msg;
					String type = m.getStringProperty("type");
					if ("request".equals(type) ) {
						lastRegistryRequest = System.currentTimeMillis();
						sendLocalOperations();
					}
					if ("operations".equals("type")) {
						String queue = m.getStringProperty("queue");
						String connection = ""; //TODO
						int cnt = 0;
						String path = null;
						synchronized (register) {
							long now = System.currentTimeMillis();
							do {
								path = m.getString("operation" + cnt);
								String version = m.getString("version" + cnt);
								cnt++;
								String ident = connection + "," + queue + "," + path + "," + version;
								OperationRegister r = register.get(ident);
								if (r == null) {
									r = new OperationRegister(connection, queue, path, version);
									register.put(ident, r);
								}
								r.setLastUpdated();
							} while (path != null);
							// remove stare
							register.entrySet().removeIf(
									entry -> entry.getValue().getQueue().equals(queue) && 
											 entry.getValue().getLastUpdated() < now
									);
						}
					}
				}
			}
			
			@Override
			public Message received(Message msg) throws JMSException {
				return null;
			}
		};
		
		timer = MApi.lookup(TimerFactory.class).getTimer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				doCheckRegistry();
			}
			
		}, MTimeInterval.MINUTE_IN_MILLISECOUNDS);
	}

	protected void doCheckRegistry() {
		if (MTimeInterval.isTimeOut(lastRegistryRequest,MTimeInterval.MINUTE_IN_MILLISECOUNDS * 3)) {
			long now = System.currentTimeMillis();
			requestOperationRegistry();
			sendLocalOperations();
			MThread.sleep(30000);
			
			// remove staled - if not updated in the last 30 seconds
			synchronized (register) {
				register.entrySet().removeIf(e -> e.getValue().getLastUpdated() < now);
			}
		}
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		if (timer != null)
			timer.cancel();
		timer = null;
		
		if (registerClient != null)
			registerClient.close();
		registerClient = null;
		if (registerServer != null)
			registerServer.close();
		registerServer = null;
		register.clear();
		
	}


	@Override
	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, IProperties parameters, AaaContext user, String ... options ) throws Exception {
		AccessApi api = MApi.lookup(AccessApi.class);
		String ticket = api.createTrustTicket(user);
		return doExecuteOperation(con, queueName, operationName, parameters, ticket, MTimeInterval.MINUTE_IN_MILLISECOUNDS / 2, options);
	}
	
	@Override
	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, IProperties parameters, String ticket, long timeout, String ... options  ) throws Exception {

		if (con == null) throw new JMSException("connection is null");
		ClientJms client = new ClientJms(con.createQueue(queueName));
		
		boolean needObject = false;
		if (!isOption(options, OPT_FORCE_MAP_MESSAGE)) {
			for (Entry<String, Object> item : parameters) {
				Object value = item.getValue();
				if (! (
						value == null || 
						value.getClass().isPrimitive() || 
						value instanceof String || 
						value instanceof Long || 
						value instanceof Integer || 
						value instanceof Boolean 
					) ) {
					needObject = true;
					break;
				}
			}
		}
		
		Message msg = null;
		if (needObject) {
			msg = con.createObjectMessage((MProperties)parameters);
		} else {
			msg = con.createMapMessage();
			for (Entry<String, Object> item : parameters) {
				String name = item.getKey();
				//if (!name.startsWith("_"))
				Object value = item.getValue();
				if (value != null && value instanceof Date) 
					value = MDate.toIsoDateTime((Date)value);
				else
				if (value != null && 
					!(value instanceof String) && !value.getClass().isPrimitive() ) 
					value = String.valueOf(value);
				((MapMessage)msg).setObject(name, value);
			}
			((MapMessage)msg).getMapNames();
		}
		
		msg.setStringProperty(Sop.PARAM_OPERATION_PATH, operationName);


		msg.setStringProperty(Sop.PARAM_AAA_TICKET, ticket );
		client.setTimeout(timeout);
    	// Send Request
    	
    	log().d(operationName,"sending Message", queueName, msg, options);
    	
    	if (!isOption(options,OPT_NEED_ANSWER)) {
    		client.sendJmsOneWay(msg);
    		return null;
    	}
    	
    	Message answer = client.sendJms(msg);

    	// Process Answer
    	
    	OperationResult out = new OperationResult();
    	out.setOperationPath(operationName);
		if (answer == null) {
			log().d(queueName,operationName,"answer is null");
			out.setSuccessful(false);
			out.setMsg("answer is null");
			out.setReturnCode(OperationResult.INTERNAL_ERROR);
		} else {
			boolean successful = answer.getBooleanProperty(Sop.PARAM_SUCCESSFUL);
			out.setSuccessful(successful);
			
			if (!successful)
				out.setMsg(answer.getStringProperty(Sop.PARAM_MSG));
			out.setReturnCode(answer.getLongProperty(Sop.PARAM_RC));
			
			if (successful) {
				
				if (answer instanceof MapMessage) {
					MapMessage mapMsg = (MapMessage)answer;
					out.setResult(MJms.getMapProperties(mapMsg));
				} else
				if (answer instanceof TextMessage) {
					out.setMsg(((TextMessage)answer).getText());
					out.setResult(out.getMsg());
				} else
				if (answer instanceof BytesMessage) {
					long len = ((BytesMessage)answer).getBodyLength();
					if (len > Sop.MAX_MSG_BYTES) {
						out.setMsg("answer bytes too long " + len);
						out.setSuccessful(false);
						out.setReturnCode(OperationResult.INTERNAL_ERROR);
					} else {
						byte[] bytes = new byte[(int) len];
						((BytesMessage)answer).readBytes(bytes);
						out.setResult(bytes);
					}
				} else
				if (answer instanceof ObjectMessage) {
					Serializable obj = ((ObjectMessage)answer).getObject();
					if (obj == null) {
						out.setResult(null);
					} else {
						out.setResult(obj);
					}
				}
			}	
		}
		
		
		client.close();
		
		return out;
	}
		
	private boolean isOption(String[] options, String opt) {
		if (options == null || opt == null) return false;
		for (String o : options)
			if (opt.equals(o)) return true;
		return false;
	}

	@Override
	public List<String>	doGetOperationList(JmsConnection con, String queueName, AaaContext user) throws Exception {
		IProperties pa = new MProperties();
		OperationResult ret = doExecuteOperation(con, queueName, "_list", pa, user, OPT_NEED_ANSWER);
		if (ret.isSuccessful()) {
			Object res = ret.getResult();
			if (res != null && res instanceof MProperties) {
				String[] list = String.valueOf( ((MProperties)res).getString("list","") ).split(",");
				LinkedList<String> out = new LinkedList<String>();
				for (String item : list) out.add(item);
				return out;
			}
		}
		return null;
	}
	
	@Override
	public List<String> lookupOperationQueues() throws Exception {
		JmsConnection con = JmsUtil.getConnection(OperationBroadcast.connectionName.value());
		ClientJms client = new ClientJms(con.createTopic(OperationBroadcast.queueName.value()));
		client.open();
		TextMessage msg = client.getSession().createTextMessage();
		LinkedList<String> out = new LinkedList<String>();
		for (Message ret : client.sendJmsBroadcast(msg)) {
			String q = ret.getStringProperty("queue");
			if (q != null)
				out.add(q);
		}
		return out;
	}

	@Override
	public void sendLocalOperations() {
		try {
			MapMessage msg = registerClient.createMapMessage();
			msg.setStringProperty("type", "operations");
			msg.setStringProperty("connection", Jms2LocalOperationExecuteChannel.connectionName.value());
			msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.queueName.value());
			
			int cnt = 0;
			
			for ( String o : MApi.lookup(LocalOperationApi.class).getOperations())
				msg.setString("operation" + (cnt++), o);
			
			registerClient.sendJms(msg);
		} catch (Throwable t) {
			log().w(t);
		}
	}

	@Override
	public void requestOperationRegistry() {
		try {
			MapMessage msg = registerClient.createMapMessage();
			msg.setStringProperty("type", "request");
			msg.setStringProperty("connection", Jms2LocalOperationExecuteChannel.connectionName.value());
			msg.setStringProperty("queue", Jms2LocalOperationExecuteChannel.queueName.value());
			registerClient.sendJms(msg);
		} catch (Throwable t) {
			log().w(t);
		}
	}

	@Override
	public List<OperationRegister> getRegisteredOperations() {
		synchronized (register) {
			return new LinkedList<OperationRegister>( register.values() );
		}
	}

	@Override
	public OperationRegister getRegisteredOperation(String path, VersionRange version) {
		synchronized (register) {
			for (OperationRegister r : register.values())
				if (r.getPath().equals(path) && version == null || version.includes(r.getVersion()))
					return r;
		}
		return null;
	}
	
}
