package de.mhus.test.bridgews.client.liferay;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;

import com.liferay.portal.kernel.servlet.SessionMessages;

import de.mhus.lib.core.MString;
import de.mhus.lib.portlet.actions.AbstractAction;
import de.mhus.osgi.jwsclient.Connection;
import de.mhus.osgi.jwsclient.web.JwsWebClient;

public class SetJwsUrlAction extends AbstractAction {

	public static final String JWS_URL_DEFAULT = "jws||http://localhost:8181/cxf/hehe?wsdl|http://impl.ws_server.ws.test.mhus.de/|WSServiceImplService";
	public static final String JWS_URL_KEY = "jws_url";
	public static final String URL_INPUT_ID = "url";
	public static final String JWS_TARGET_NAME = "";

	@Override
	public boolean processAction(String path, ActionRequest request,
			ActionResponse response) throws Exception {

		String curUrl = getCurrentJwsUrl(request);
		String newUrl = createProperties(request).getString(URL_INPUT_ID, JWS_URL_DEFAULT);

		if (!MString.equals(curUrl, newUrl)) {
			request.getPreferences().setValue(JWS_URL_KEY, newUrl);
			request.getPreferences().store();
			
			JwsWebClient client = JwsWebClient.instance();
			client.closeConnection(request.getPortletSession(), JWS_TARGET_NAME);
			client.closeTarget(JWS_TARGET_NAME);
			
			SessionMessages.add(request, "request_processed", "Der Wert ["+newUrl+"] wurde gespeichert.");

		}

		response.setPortletMode(PortletMode.VIEW);
		
		return true;
	}

	public static String getCurrentJwsUrl(PortletRequest request) {
		return request.getPreferences().getValue(JWS_URL_KEY, JWS_URL_DEFAULT);
	}
	
}
