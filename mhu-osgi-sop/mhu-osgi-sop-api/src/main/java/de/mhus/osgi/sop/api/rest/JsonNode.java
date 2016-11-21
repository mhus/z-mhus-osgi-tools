package de.mhus.osgi.sop.api.rest;

import javax.transaction.NotSupportedException;

import de.mhus.osgi.sop.api.action.ActionDescriptor;

public abstract class JsonNode<T> extends AbstractNode<T>{

	@Override
	public RestResult doRead(CallContext callContext) throws Exception {
		JsonResult result = new JsonResult();
		doRead(result, callContext);
		return result;
	}

	public abstract void doRead(JsonResult result, CallContext callContext) throws Exception;

	@Override
	public RestResult doCreate(CallContext callContext) throws Exception {
		ActionDescriptor oper = getCreateBpmOperation();
		if (oper != null)
			return super.doCreate(callContext);
		JsonResult result = new JsonResult();
		doCreate(result, callContext);
		return result;
	}

	@Override
	public RestResult doUpdate(CallContext callContext) throws Exception {
		ActionDescriptor oper = getUpdateBpmOperation();
		if (oper != null)
			return super.doUpdate(callContext);
		JsonResult result = new JsonResult();
		doUpdate(result, callContext);
		return result;
	}

	@Override
	public RestResult doDelete(CallContext callContext) throws Exception {
		ActionDescriptor oper = getDeleteBpmOperation();
		if (oper != null)
			return super.doDelete(callContext);
		JsonResult result = new JsonResult();
		doDelete(result, callContext);
		return result;
	}
	
	public void doUpdate(JsonResult result, CallContext callContext) throws Exception {
		throw new NotSupportedException();
	}
	public void doCreate(JsonResult result, CallContext callContext) throws Exception {
		throw new NotSupportedException();
	}
	public void doDelete(JsonResult result, CallContext callContext) throws Exception {
		throw new NotSupportedException();
	}
	
	/*		
	@Override
	public RestResult doAction(CallContext callContext) throws Exception {
		String methodName = "on" + MPojo.toFunctionName(callContext.getAction(), true, null);
		JsonResult result = new JsonResult();
		Method method = getClass().getMethod(methodName, JsonResult.class, CallContext.class);
		method.invoke(this, result, callContext);
		return result;
		
	}
 */		

}
