package de.mhus.test.bridgews.client.liferay;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.portlet.resource.AjaxResource;
import de.mhus.test.ws.ws_client.web.Connection;
import de.mhus.test.ws.ws_client.web.Singleton;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;

public class AjaxService extends AjaxResource {

	@Override
	protected void doRequest(ResourceRequest request, JsonGenerator out)
			throws IOException, PortletException {

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		boolean done = false;
		try {
			Singleton.get().setUrlName("http://localhost:8181/cxf/hehe?wsdl");

			IProperties prop = createProperties(request);
			String action = prop.getString("action", "");

			if (action.equals("list")) {
				ArrayNode result = node.putArray("result");
	
				Connection con = Singleton.get().getConnection(request.getPortletSession());
				WSService service = con.getWSService();
				for (WSEntity entity : service.getAll()) {
					ObjectNode entry = result.addObject();
					entry.put("name", entity.getName());
				}
				done = true;
				
			} else
			if (action.equals("remove")) {
				String name = prop.getString("name", null);
				if (name == null) throw new NullPointerException("name is not set");

				Connection con = Singleton.get().getConnection(request.getPortletSession());
				WSService service = con.getWSService();
				
				// OK this is a trick, but it works for this demo !
				WSEntity entry = new WSEntity();
				entry.setName(name);
				service.remvoeEntity(entry);
				done = true;
				
			} else
			if (action.equals("add")) {
				
				String name = prop.getString("name", null);
				if (name == null) throw new NullPointerException("name is not set");

				Connection con = Singleton.get().getConnection(request.getPortletSession());
				WSService service = con.getWSService();
				
				WSEntity entry = new WSEntity();
				entry.setName(name);
				service.addEntity(entry);
				done = true;
				
			}
			
			
		} catch (Throwable t) {
			t.printStackTrace();
			node.put("error", "Fehler: " + t.toString());
		}
		
		node.put("success", done ? 1 : 0);

		out.setCodec(mapper);
		out.writeTree(node);

	}

}
