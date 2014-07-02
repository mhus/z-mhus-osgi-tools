package de.mhus.test.bridgews.client.liferay;

import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.portlet.callback.AbstractAjaxCallback;
import de.mhus.lib.portlet.callback.AjaxResponse;
import de.mhus.osgi.jwsclient.Connection;
import de.mhus.osgi.jwsclient.web.JwsWebClient;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;

public class AjaxService extends AbstractAjaxCallback {

	@Override
	protected void doRequest(IProperties properties, AjaxResponse response) throws Exception{
		String action = properties.getString("action", "");

		JwsWebClient client = JwsWebClient.instance();
		client.createTarget(SetJwsUrlAction.getCurrentJwsUrl(response.getRequest()));
		
		Connection con = client.getConnection(response.getRequest().getPortletSession(), SetJwsUrlAction.JWS_TARGET_NAME);
		WSService service = con.getService("WSServiceImplService", WSService.class);
		
		response.setSuccess(false);
		
		if (action.equals("list")) {

			for (WSEntity entity : service.getAll()) {
				ObjectNode entry = response.addResult();
				entry.put("name", entity.getName());
			}
			response.setSuccess(true);
			
		} else
		if (action.equals("remove")) {
			String name = properties.getString("name", null);
			if (name == null) throw new NullPointerException("name is not set");
			name = name.trim();
			if (name.length() == 0) throw new NullPointerException("name is empty");

			// OK this is a trick, but it works for this demo !
			WSEntity entry = new WSEntity();
			entry.setName(name);
			service.removeEntity(entry);
			response.addSuccess("removed=Item Removed");
			response.setSuccess(true);
		} else
		if (action.equals("add")) {
			
			String name = properties.getString("name", null);
			if (name == null) throw new NullPointerException("name is not set");
			name = name.trim();
			if (name.length() == 0) throw new NullPointerException("name is empty");

			WSEntity entry = new WSEntity();
			entry.setName(name);
			service.addEntity(entry);
			response.addSuccess("created=Item Created");
			response.setSuccess(true);
			
		}
		
	}

}
