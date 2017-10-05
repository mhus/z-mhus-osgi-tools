package de.mhus.osgi.sop.api.jms;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.osgi.service.component.ComponentContext;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.pojo.DefaultFilter;
import de.mhus.lib.core.pojo.PojoAttribute;
import de.mhus.lib.core.pojo.PojoModel;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.pojo.PojoParser;
import de.mhus.lib.core.strategy.NotSuccessful;
import de.mhus.lib.core.strategy.OperationDescription;
import de.mhus.lib.core.strategy.OperationResult;
import de.mhus.lib.core.strategy.Successful;
import de.mhus.lib.jms.MJms;
import de.mhus.lib.jms.ServerJms;
import de.mhus.lib.karaf.jms.JmsDataChannelImpl;
import de.mhus.osgi.sop.api.Sop;

/**
 * The class implement the protocol to provide a 'operation' connection via JMS. It's the
 * backend of operation connectors e.g. in Bonitasoft or the admin interface.
 * 
 * 
 * @author mikehummel
 *
 */
public abstract class AbstractJmsOperationExecuteChannel extends JmsDataChannelImpl {


	public void doActivate(ComponentContext ctx) {
		
		setDestination(getQueueName());
		setDestinationTopic(false);
		setChannel(null);
		setConnectionName(getJmsConnectionName());
		setName(getServiceName());
		reset();
//		server.setInterceptorIn(new TicketAccessInterceptor());
	}	
	
	public void doDeactivate(ComponentContext ctx) {
		if (getServer() != null) getServer().close();
		setChannel(null);
	}

	protected ServerJms getServer() {
		return (ServerJms) getChannel();
	};
	
	protected Message received(Message msg) throws JMSException {
		
		String path = msg.getStringProperty(Sop.PARAM_OPERATION_PATH);
		if (path == null) return null;
		IProperties properties = null;
		if (msg instanceof MapMessage) {
			properties = MJms.getMapProperties((MapMessage)msg);
		} else
		if (msg instanceof ObjectMessage) {
			Serializable obj = ((ObjectMessage)msg).getObject();
			if (obj == null) {
				
			} else
			if (obj instanceof MProperties) {
				properties = (MProperties)obj;
			} else
			if (obj instanceof Map) {
				properties = new MProperties( (Map)obj );
			}
		}
		
		if (properties == null)
			properties = new MProperties(); // empty
		
		OperationResult res = null;
		if (path.equals(Sop.OPERATION_LIST)) {
			String list = MString.join(getPublicOperations().iterator(), ",");
			res = new Successful(Sop.OPERATION_LIST, "list",OperationResult.OK,"list",list);
		} else
		if (path.equals(Sop.OPERATION_INFO)) {
			String id = properties.getString(Sop.PARAM_OPERATION_ID, null);
			if (id == null) 
				res = new NotSuccessful(Sop.OPERATION_INFO, "not found", OperationResult.NOT_FOUND);
			else {
				OperationDescription des = getOperationDescription(id);
				if (des == null)
					res = new NotSuccessful(Sop.OPERATION_INFO, "not found", OperationResult.NOT_FOUND);
				else {
					res = new Successful(Sop.OPERATION_INFO, "list",OperationResult.OK,
							"group",des.getGroup(),
							"id",des.getId(),
							"form",des.getForm().toString(),
							"title",des.getCaption()
							);
				}
			}
		} else
			res = doExecute(path, properties);
		
		Message ret = null;
		boolean consumed = false;
		if (res != null && res.getResult() != null && res.getResult() instanceof Map) {
			// Map Message is allowed if all values are primitives. If not use object Message
			consumed = true;
			ret = getServer().createMapMessage();
			Map<?,?> map = (Map<?,?>)res.getResult();
			for (Map.Entry<?,?> entry : map.entrySet()) {
				Object value = entry.getValue();
				if (value == null || value.getClass().isPrimitive() || value instanceof String )
					((MapMessage)ret).setObject(String.valueOf(entry.getKey()), entry.getValue() );
				else {
					consumed = false;
					break;
				}
			}
		}
		
		if (consumed) {
			// already done
		} else
		if (res != null && res.getResult() != null && res.getResult() instanceof Serializable ) {
			ret = getServer().createObjectMessage();
			((ObjectMessage)ret).setObject((Serializable) res.getResult());
		} else
		if (res != null && res.getResult() != null) {
			ret = getServer().createMapMessage();
			try {
				IProperties prop = pojoToProperties(res.getResult(), new PojoModelFactory() {
					
					@Override
					public PojoModel createPojoModel(Class<?> pojoClass) {
						PojoModel model = new PojoParser().parse(pojoClass,"_",null).filter(new DefaultFilter(true, false, false, false, true) ).getModel();
						return model;
					}
				} );
				MJms.setMapProperties(prop, (MapMessage)ret);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //TODO MPojo.pojoToProperties();
		} else {
			ret = getServer().createTextMessage(null);
		}
		
		if (res == null) {
			ret.setLongProperty("rc", OperationResult.INTERNAL_ERROR);
			ret.setStringProperty("msg", "null");
			ret.setBooleanProperty("successful", false);
		} else {
			ret.setLongProperty("rc", res.getReturnCode());
			ret.setStringProperty("msg", res.getMsg());
			ret.setBooleanProperty("successful", res.isSuccessful());
			OperationDescription next = res.getNextOperation();
			if (next != null) {
				ret.setStringProperty("next.path", next.getPath());
				MProperties prop = new MProperties(next.getParameters());
				MJms.setProperties("next.p.", prop, ret);
			}
		}
		ret.setStringProperty("path", path);
		
		return ret;
	}
	
	@Deprecated // use MPojo instead
	public static IProperties pojoToProperties(Object from, PojoModelFactory factory) throws IOException {
		MProperties out = new MProperties();
		PojoModel model = factory.createPojoModel(from.getClass());

		for (PojoAttribute<?> attr : model) {
			Object value = attr.get(from);
			String name = attr.getName();
			Class<?> type = attr.getType();
			if (type == int.class) out.setInt(name, (int)value);
			else
			if (type == Integer.class) out.setInt(name, (Integer)value);
			else
			if (type == long.class)  out.setLong(name, (long)value);
			else
			if (type == Long.class)  out.setLong(name, (Long)value);
			else
			if (type == float.class)  out.setFloat(name, (float)value);
			else
			if (type == Float.class)  out.setFloat(name, (Float)value);
			else
			if (type == double.class)  out.setDouble(name, (double)value);
			else
			if (type == Double.class)  out.setDouble(name, (Double)value);
			else
			if (type == boolean.class)  out.setBoolean(name, (boolean)value);
			else
			if (type == Boolean.class)  out.setBoolean(name, (Boolean)value);
			else
			if (type == String.class)  out.setString(name, (String)value);
			else
			if (type == Date.class)  out.setDate(name, (Date)value);
			else
				out.setString(name, String.valueOf(value));
		}
		return out;
	}

	protected String getServiceName() {
		return getClass().getCanonicalName();
	}

	/**
	 * Return the name of the queue the service is reachable in the JMS universe.
	 * The method is called once starting the service.
	 * @return The name of the queue - must be unique in the JMS universe.
	 */
	protected abstract String getQueueName();
	
	/**
	 * Return the name of the jms connection which the service should be bound.
	 * The method is called once starting the service.
	 * @return The name of the connection, configured in the framework (jms:connection-list).
	 */
	protected abstract String getJmsConnectionName();

	/**
	 * This event is triggered if the operation is called. Execute the command and return a
	 * result to inform the caller.
	 * 
	 * @param path The name of the command
	 * @param properties properties given.
	 * @return
	 */
	protected abstract OperationResult doExecute(String path, IProperties properties);

	/**
	 * Return a list of current possible and public operations. The list can be called
	 * periodically and return another set of operation paths depending on the
	 * current situation.
	 * 
	 * It's a lightweight information about the operations. You only need 
	 * to implement it if you wan't to allow introspection of the channel.
	 * 
	 * @return A list of operation paths.
	 */
	protected abstract List<String> getPublicOperations();
	
	/**
	 * Return the description of the operation with the specified path.
	 * It's a lightweight information about the operations. You only need 
	 * to implement it if you wan't to allow introspection of the channel.
	 * 
	 * @param path The path to the operation
	 * @return The description or null.
	 */
	protected abstract OperationDescription getOperationDescription(String path);
	
}
