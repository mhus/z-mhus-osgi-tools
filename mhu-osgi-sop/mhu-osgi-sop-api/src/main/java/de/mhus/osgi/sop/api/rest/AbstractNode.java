package de.mhus.osgi.sop.api.rest;

import java.util.List;

import javax.transaction.NotSupportedException;

import de.mhus.osgi.sop.api.action.ActionDescriptor;

public abstract class AbstractNode<T> implements RestNodeService {

	public static final String ID 		= "_id";
	public static final String OBJECT 	= "_obj";
	public static final String SOURCE 	= "source";
	public static final String INTERNAL_PREFIX = "_";

	@Override
	public Node lookup(List<String> parts, CallContext callContext)
			throws Exception {
		if (parts.size() < 1) return this;

		String id = parts.get(0);
		parts.remove(0);
		
		T obj = getObjectForId(id);

		if (obj == null) return null;
		
		callContext.put(getManagedClass().getCanonicalName() + ID, id);
		callContext.put(getManagedClass().getCanonicalName() + OBJECT, obj);

		if (parts.size() < 1) return this;

		return callContext.lookup(parts, getNodeId());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getObjectFromContext(CallContext callContext, Class<T> clazz) {
		return (T) callContext.get(clazz.getCanonicalName() + OBJECT);
	}

	@SuppressWarnings("unchecked")
	protected T getObjectFromContext(CallContext callContext) {
		return (T) callContext.get(getManagedClass().getCanonicalName() + OBJECT);
	}
 
	/**
	 * Return a the managed class as class
	 * @return x
	 */
	public abstract Class<T> getManagedClass();

	protected String getIdFromContext(CallContext callContext) {
		return (String) callContext.get(getManagedClass().getCanonicalName() + ID);
	}
	
	public static <T> String getIdFromContext(CallContext callContext, Class<T> clazz) {
		return (String) callContext.get(clazz.getCanonicalName() + ID);
	}
	
	protected abstract T getObjectForId(String id) throws Exception;

	protected abstract ActionDescriptor getCreateBpmOperation();

	protected abstract ActionDescriptor getUpdateBpmOperation();
	
	protected abstract ActionDescriptor getDeleteBpmOperation();

	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
/*
		String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
		Method method = getClass().getMethod(methodName, CallContext.class);
		return (RestResult) method.invoke(this, callContext);
*/
		return RestUtil.doExecuteBpm((String)null, callContext, getNodeId());
	}

	@Override
	public RestResult doCreate(CallContext callContext) throws Exception {
//		JsonResult result = new JsonResult();
//		doCreate(result, callContext);
//		return result;
		ActionDescriptor oper = getCreateBpmOperation();
		if (oper == null)
			throw new NotSupportedException();
		return RestUtil.doExecuteBpm( oper, callContext, getNodeId());
	}

	@Override
	public RestResult doUpdate(CallContext callContext) throws Exception {
//		JsonResult result = new JsonResult();
//		doUpdate(result, callContext);
//		return result;
		ActionDescriptor oper = getUpdateBpmOperation();
		if (oper == null)
			throw new NotSupportedException();
		return RestUtil.doExecuteBpm( oper, callContext, getNodeId());
	}


	@Override
	public RestResult doDelete(CallContext callContext) throws Exception {
//		JsonResult result = new JsonResult();
//		doDelete(result, callContext);
//		return result;
		ActionDescriptor oper = getDeleteBpmOperation();
		if (oper == null)
			throw new NotSupportedException();
		return RestUtil.doExecuteBpm( oper, callContext, getNodeId());
	}

}
