package de.mhus.osgi.sop.impl.operation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MTimeInterval;
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VectorMap;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.jms.JmsObject;
import de.mhus.lib.jms.MJms;
import de.mhus.lib.karaf.jms.JmsUtil;
import de.mhus.osgi.sop.api.Sop;
import de.mhus.osgi.sop.api.SopApi;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.operation.OperationApi;
import de.mhus.osgi.sop.api.operation.OperationBpmDefinition;
import de.mhus.osgi.sop.api.operation.OperationException;
import de.mhus.osgi.sop.api.operation.OperationService;

@Component(immediate=true)
public class OperationApiImpl extends MLog implements OperationApi {

	private BundleContext context;
	private ServiceTracker<OperationService,OperationService> nodeTracker;
	private HashMap<String, OperationService> register = new HashMap<>();
	private VectorMap<String, String, OperationService> groups = new VectorMap<>();
	private HashMap<String, OperationService> bpmRegister = new HashMap<>();
	

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		nodeTracker = new ServiceTracker<>(context, OperationService.class, new MyServiceTrackerCustomizer() );
		nodeTracker.open();
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		context = null;
	}

	private class MyServiceTrackerCustomizer implements ServiceTrackerCustomizer<OperationService,OperationService> {

		@Override
		public OperationService addingService(
				ServiceReference<OperationService> reference) {

			OperationService service = context.getService(reference);
			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("register",desc);
					synchronized (register) {
						if (register.put(desc.getPath(), service) != null)
							log().w("Operation already defined",desc.getPath());
						groups.put(desc.getGroup(), desc.getId(), service);
						OperationBpmDefinition bpmDef = service.getBpmDefinition();
						if (bpmDef != null) {
							service.getBpmDefinition().setService(service);
							if (bpmRegister.put(bpmDef.getRegisterName(), service) != null) log().w("BpmDefinition already registered",bpmDef.getRegisterName());
						}
					}
				} else {
					log().i("no description found, not registered",reference.getProperty("objectClass"));
				}
			}
			return service;
		}

		@Override
		public void modifiedService(
				ServiceReference<OperationService> reference,
				OperationService service) {

			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("modified",desc);
					synchronized (register) {
						register.put(desc.getPath(), service);
						groups.put(desc.getGroup(), desc.getId(), service);
						OperationBpmDefinition bpmDef = service.getBpmDefinition();
						if (bpmDef != null) {
							service.getBpmDefinition().setService(service);
							bpmRegister.put(bpmDef.getRegisterName(), service);
						}
					}
				}
			}
			
		}

		@Override
		public void removedService(
				ServiceReference<OperationService> reference,
				OperationService service) {
			
			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("unregister",desc);
					synchronized (register) {
						register.remove(desc.getPath());
						groups.removeValue(desc.getGroup(), desc.getId());
						OperationBpmDefinition bpmDef = service.getBpmDefinition();
						if (bpmDef != null) bpmRegister.remove(bpmDef.getRegisterName());
					}
				}
			}			
		}
		
	}

	@Override
	public String[] getGroups() {
		synchronized (register) {
			return groups.keySet().toArray(new String[0]);
		}
	}

	@Override
	public String[] getOperations() {
		synchronized (register) {
			return register.keySet().toArray(new String[0]);
		}
	}

	@Override
	public String[] getOperationForGroup(String group) {
		synchronized (register) {
			return groups.get(group).keySet().toArray(new String[0]);
		}
	}

	@Override
	public Operation getOperation(String path) {
		synchronized (register) {
			return register.get(path);
		}
	}

	@Override
	public OperationResult doExecute(String path, IProperties properties) {
		
		int p = path.indexOf('/');
		if (p >= 0) {
			String queue = path.substring(0,p);
			path = path.substring(p+1);
			SopApi access = Sop.getApi(SopApi.class);
			try {
				OperationResult answer = doExecuteOperation(Sop.getDefaultJmsConnection(), queue, path, properties, access.getCurrent(), true);
				return answer;
			} catch (Exception e) {
				log().w(path,e);
				return null;
			}
		}
		
		Operation operation = getOperation(path);
		if (operation == null) return new NotSuccessful(path, "operation not found", OperationResult.NOT_FOUND);
		
		DefaultTaskContext taskContext = new DefaultTaskContext();
		taskContext.setParameters(properties);
		try {
			return operation.doExecute(taskContext);
		} catch (OperationException e) {
			log().w(path,properties,e);
			return new NotSuccessful(path,e.getMessage(), e.getReturnCode());
		} catch (Exception e) {
			log().w(path,properties,e);
			return new NotSuccessful(path,e.toString(), OperationResult.INTERNAL_ERROR);
		}
	}

	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, IProperties parameters, AaaContext user, boolean needAnswer ) throws Exception {
		SopApi api = Sop.getApi(SopApi.class);
		String ticket = api.createTrustTicket(user);
		return doExecuteOperation(con, queueName, operationName, parameters, ticket, MTimeInterval.MINUTE_IN_MILLISECOUNDS / 2, needAnswer);
	}
	
	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, IProperties parameters, String ticket, long timeout, boolean needAnswer ) throws Exception {

		if (con == null) throw new JMSException("connection is null");
		ClientJms client = new ClientJms(con.createQueue(queueName));
				
		boolean needObject = false;
		for (Entry<String, Object> item : parameters) {
			Object value = item.getValue();
			if (! (value == null || value.getClass().isPrimitive() || value instanceof String ) ) {
				needObject = true;
				break;
			}
		}
		
		Message msg = null;
		if (needObject) {
			msg = con.createObjectMessage((MProperties)parameters);
		} else {
			msg = con.createMapMessage();
			for (Entry<String, Object> item : parameters) {
				//String name = item.getKey();
				//if (!name.startsWith("_"))
				((MapMessage)msg).setObject(item.getKey(), item.getValue()); //TODO different types, currently it's only String ?!
			}
			((MapMessage)msg).getMapNames();
		}
		
		msg.setStringProperty(Sop.PARAM_OPERATION_PATH, operationName);


		msg.setStringProperty(Sop.PARAM_AAA_TICKET, ticket );
		client.setTimeout(timeout);
    	// Send Request
    	
    	log().d(operationName,"sending Message", queueName, msg, needAnswer);
    	
    	if (!needAnswer) {
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
		
	public List<String>	doGetOperationList(JmsConnection con, String queueName, AaaContext user) throws Exception {
		IProperties pa = new MProperties();
		OperationResult ret = doExecuteOperation(con, queueName, "_list", pa, user, true);
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
	public OperationBpmDefinition getActionDefinition(String prozess) {
		synchronized (register) {
			OperationService o = bpmRegister.get(prozess);
			return o == null ? null : o.getBpmDefinition();
		}
	}

	@Override
	public List<OperationBpmDefinition> getActionDefinitions() {
		synchronized (register) {
			LinkedList<OperationBpmDefinition> out = new LinkedList<OperationBpmDefinition>();
			for (OperationService service : bpmRegister.values())
				out.add(service.getBpmDefinition());
			return out;
		}
	}

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
	
}
