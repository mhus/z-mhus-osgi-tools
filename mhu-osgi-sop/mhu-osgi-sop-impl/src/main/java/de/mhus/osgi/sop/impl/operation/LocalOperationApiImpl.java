package de.mhus.osgi.sop.impl.operation;

import java.util.HashMap;
import java.util.LinkedList;

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
import de.mhus.lib.core.strategy.DefaultTaskContext;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.Operation;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.util.VectorMap;
import de.mhus.osgi.sop.api.operation.LocalOperationApi;
import de.mhus.osgi.sop.api.operation.OperationDescriptor;
import de.mhus.osgi.sop.api.operation.OperationException;
import de.mhus.osgi.sop.api.operation.OperationsProvider;

@Component(immediate=true,provide={LocalOperationApi.class, OperationsProvider.class})
public class LocalOperationApiImpl extends MLog implements LocalOperationApi, OperationsProvider {

	private BundleContext context;
	private ServiceTracker<Operation,Operation> nodeTracker;
	private HashMap<String, OperationDescriptor> register = new HashMap<>();
	private VectorMap<String, String, OperationDescriptor> groups = new VectorMap<>();
	public static LocalOperationApiImpl instance;

	@Activate
	public void doActivate(ComponentContext ctx) {
		context = ctx.getBundleContext();
		nodeTracker = new ServiceTracker<>(context, Operation.class, new MyServiceTrackerCustomizer() );
		nodeTracker.open();
		instance = this;
	}

	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		instance  = null;
		context = null;
	}

	private class MyServiceTrackerCustomizer implements ServiceTrackerCustomizer<Operation,Operation> {

		@Override
		public Operation addingService(
				ServiceReference<Operation> reference) {

			Operation service = context.getService(reference);
			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("register",desc);
					synchronized (register) {
						OperationDescriptor descriptor = createDescriptor(reference, service);
						if (register.put(desc.getPath(), descriptor ) != null)
							log().w("Operation already defined",desc.getPath());
						groups.put(desc.getGroup(), desc.getId(), descriptor);
					}
				} else {
					log().i("no description found, not registered",reference.getProperty("objectClass"));
				}
			}
			return service;
		}

		private OperationDescriptor createDescriptor(ServiceReference<Operation> reference, Operation service) {
			String source = "operation";
			LinkedList<String> tags = new LinkedList<>();
			Object tagsStr = reference.getProperty("tags");
			if (tagsStr instanceof String[]) {
				for (String item : (String[])tagsStr)
					tags.add(item);
			} else
			if (tagsStr instanceof String) {
				for (String item : ((String)tagsStr).split(","))
					tags.add(item);
			}
			service.getDescription().getForm();
			OperationDescription desc = service.getDescription();
			return new OperationDescriptor(
					service, 
					tags, 
					source, 
					desc != null ? desc.getParameterDefinitions() : null,
					desc != null ? desc.getForm() : null,
					service,
					desc != null ? desc.getTitle() : source
					);
		}

		@Override
		public void modifiedService(
				ServiceReference<Operation> reference,
				Operation service) {

			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("modified",desc);
					synchronized (register) {
						OperationDescriptor descriptor = createDescriptor(reference, service);
						register.put(desc.getPath(), descriptor);
						groups.put(desc.getGroup(), desc.getId(), descriptor);
					}
				}
			}
			
		}

		@Override
		public void removedService(
				ServiceReference<Operation> reference,
				Operation service) {
			
			if (service != null) {
				OperationDescription desc = service.getDescription();
				if (desc != null && desc.getPath() != null) {
					log().i("unregister",desc);
					synchronized (register) {
						register.remove(desc.getPath());
						groups.removeValue(desc.getGroup(), desc.getId());
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
	public OperationDescriptor getOperation(String path) {
		synchronized (register) {
			return register.get(path);
		}
	}

	@Override
	public OperationResult doExecute(String path, IProperties properties) {
		
//		int p = path.indexOf('/');
//		if (p >= 0) {
//			String queue = path.substring(0,p);
//			path = path.substring(p+1);
//			AccessApi access = Sop.getApi(AccessApi.class);
//			try {
//				OperationResult answer = doExecuteOperation(Sop.getDefaultJmsConnection(), queue, path, properties, access.getCurrentOrGuest(), true);
//				return answer;
//			} catch (Exception e) {
//				log().w(path,e);
//				return null;
//			}
//		}
		
		OperationDescriptor operation = getOperation(path);
		if (operation == null) return new NotSuccessful(path, "operation not found", OperationResult.NOT_FOUND);
		
		DefaultTaskContext taskContext = new DefaultTaskContext(getClass());
		taskContext.setParameters(properties);
		try {
			return operation.getOperation().doExecute(taskContext);
		} catch (OperationException e) {
			log().w(path,properties,e);
			return new NotSuccessful(path,e.getMessage(), e.getReturnCode());
		} catch (Exception e) {
			log().w(path,properties,e);
			return new NotSuccessful(path,e.toString(), OperationResult.INTERNAL_ERROR);
		}
	}

//	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, IProperties parameters, AaaContext user, boolean needAnswer ) throws Exception {
//		AccessApi api = Sop.getApi(AccessApi.class);
//		String ticket = api.createTrustTicket(user);
//		return doExecuteOperation(con, queueName, operationName, parameters, ticket, MTimeInterval.MINUTE_IN_MILLISECOUNDS / 2, needAnswer);
//	}
//	
//	public OperationResult doExecuteOperation(JmsConnection con, String queueName, String operationName, IProperties parameters, String ticket, long timeout, boolean needAnswer ) throws Exception {
//
//		if (con == null) throw new JMSException("connection is null");
//		ClientJms client = new ClientJms(con.createQueue(queueName));
//				
//		boolean needObject = false;
//		for (Entry<String, Object> item : parameters) {
//			Object value = item.getValue();
//			if (! (value == null || value.getClass().isPrimitive() || value instanceof String ) ) {
//				needObject = true;
//				break;
//			}
//		}
//		
//		Message msg = null;
//		if (needObject) {
//			msg = con.createObjectMessage((MProperties)parameters);
//		} else {
//			msg = con.createMapMessage();
//			for (Entry<String, Object> item : parameters) {
//				//String name = item.getKey();
//				//if (!name.startsWith("_"))
//				((MapMessage)msg).setObject(item.getKey(), item.getValue()); //TODO different types, currently it's only String ?!
//			}
//			((MapMessage)msg).getMapNames();
//		}
//		
//		msg.setStringProperty(Sop.PARAM_OPERATION_PATH, operationName);
//
//
//		msg.setStringProperty(Sop.PARAM_AAA_TICKET, ticket );
//		client.setTimeout(timeout);
//    	// Send Request
//    	
//    	log().d(operationName,"sending Message", queueName, msg, needAnswer);
//    	
//    	if (!needAnswer) {
//    		client.sendJmsOneWay(msg);
//    		return null;
//    	}
//    	
//    	Message answer = client.sendJms(msg);
//
//    	// Process Answer
//    	
//    	OperationResult out = new OperationResult();
//    	out.setOperationPath(operationName);
//		if (answer == null) {
//			log().d(queueName,operationName,"answer is null");
//			out.setSuccessful(false);
//			out.setMsg("answer is null");
//			out.setReturnCode(OperationResult.INTERNAL_ERROR);
//		} else {
//			boolean successful = answer.getBooleanProperty(Sop.PARAM_SUCCESSFUL);
//			out.setSuccessful(successful);
//			
//			if (!successful)
//				out.setMsg(answer.getStringProperty(Sop.PARAM_MSG));
//			out.setReturnCode(answer.getLongProperty(Sop.PARAM_RC));
//			
//			if (successful) {
//				
//				if (answer instanceof MapMessage) {
//					MapMessage mapMsg = (MapMessage)answer;
//					out.setResult(MJms.getMapProperties(mapMsg));
//				} else
//				if (answer instanceof TextMessage) {
//					out.setMsg(((TextMessage)answer).getText());
//					out.setResult(out.getMsg());
//				} else
//				if (answer instanceof BytesMessage) {
//					long len = ((BytesMessage)answer).getBodyLength();
//					if (len > Sop.MAX_MSG_BYTES) {
//						out.setMsg("answer bytes too long " + len);
//						out.setSuccessful(false);
//						out.setReturnCode(OperationResult.INTERNAL_ERROR);
//					} else {
//						byte[] bytes = new byte[(int) len];
//						((BytesMessage)answer).readBytes(bytes);
//						out.setResult(bytes);
//					}
//				} else
//				if (answer instanceof ObjectMessage) {
//					Serializable obj = ((ObjectMessage)answer).getObject();
//					if (obj == null) {
//						out.setResult(null);
//					} else {
//						out.setResult(obj);
//					}
//				}
//			}	
//		}
//		
//		
//		client.close();
//		
//		return out;
//	}
		
//	public List<String>	doGetOperationList(JmsConnection con, String queueName, AaaContext user) throws Exception {
//		IProperties pa = new MProperties();
//		OperationResult ret = doExecuteOperation(con, queueName, "_list", pa, user, true);
//		if (ret.isSuccessful()) {
//			Object res = ret.getResult();
//			if (res != null && res instanceof MProperties) {
//				String[] list = String.valueOf( ((MProperties)res).getString("list","") ).split(",");
//				LinkedList<String> out = new LinkedList<String>();
//				for (String item : list) out.add(item);
//				return out;
//			}
//		}
//		return null;
//	}
	
	// Deprecated
//	public List<String> lookupOperationQueues() throws Exception {
//		JmsConnection con = JmsUtil.getConnection(OperationBroadcast.connectionName.value());
//		ClientJms client = new ClientJms(con.createTopic(OperationBroadcast.queueName.value()));
//		client.open();
//		TextMessage msg = client.getSession().createTextMessage();
//		LinkedList<String> out = new LinkedList<String>();
//		for (Message ret : client.sendJmsBroadcast(msg)) {
//			String q = ret.getStringProperty("queue");
//			if (q != null)
//				out.add(q);
//		}
//		return out;
//	}
	
}
