package de.mhus.test.bridgews.client.liferay;

import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.portlet.callback.AbstractAjaxCallback;
import de.mhus.lib.portlet.callback.CallContext;
import de.mhus.osgi.jwsclient.Connection;
import de.mhus.osgi.jwsclient.web.JwsWebClient;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;

public class AjaxService extends AbstractAjaxCallback {

	@Override
	protected void doRequest(CallContext context) throws Exception{
		String action = context.getProperties().getString("action", "");

		JwsWebClient client = JwsWebClient.instance();
		client.createTarget(SetJwsUrlAction.getCurrentJwsUrl(context.getRequest()));
		
		Connection con = client.getConnection(context.getRequest().getPortletSession(), SetJwsUrlAction.JWS_TARGET_NAME);
		WSService service = con.getService("WSServiceImplService", WSService.class);
		
		context.setSuccess(false);
		
		if (action.equals("list")) {

			for (WSEntity entity : service.getAll()) {
				ObjectNode entry = context.addResult();
				entry.put("name", entity.getName());
			}
			context.setSuccess(true);
			
		} else
		if (action.equals("remove")) {
			String name = context.getProperties().getString("name", null);
			if (name == null) throw new NullPointerException("name is not set");
			name = name.trim();
			if (name.length() == 0) throw new NullPointerException("name is empty");

			// OK this is a trick, but it works for this demo !
			WSEntity entry = new WSEntity();
			entry.setName(name);
			service.removeEntity(entry);
			context.addSuccess("removed=Item Removed");
			context.setSuccess(true);
		} else
		if (action.equals("add")) {
			
			String name = context.getProperties().getString("name", null);
			if (name == null) throw new NullPointerException("name is not set");
			name = name.trim();
			if (name.length() == 0) throw new NullPointerException("name is empty");

			WSEntity entry = new WSEntity();
			entry.setName(name);
			service.addEntity(entry);
			context.addSuccess("created=Item Created");
			context.setSuccess(true);
			
		}
		
	}

}
