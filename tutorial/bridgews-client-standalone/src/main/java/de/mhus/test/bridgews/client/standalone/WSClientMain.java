package de.mhus.test.bridgews.client.standalone;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

import org.apache.cxf.frontend.ClientProxy;

import de.mhus.lib.core.MArgs;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MStopWatch;
import de.mhus.osgi.jwsclient.Client;
import de.mhus.osgi.jwsclient.Connection;
import de.mhus.osgi.jwsclient.Target;
import de.mhus.osgi.jwsclient.standalone.JwsStandaloneClient;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;

public class WSClientMain {

	public static void main(String[] args) throws IOException {

		MArgs a = new MArgs(args);
		String[] defaultUrls = {
				"jws|bridews-server|http://localhost:8181/cxf/itsabridge?wsdl|http://bridgews_server.bridgews.test.mhus.de/|BridgeWsServerService"
				,
				"jws|ws-server|http://localhost:8181/cxf/hehe?wsdl|http://impl.ws_server.ws.test.mhus.de/|WSServiceImplService"
		};
		
		String[] urls = a.getValues("url");
		if (urls == null || urls.length == 0)
			urls = defaultUrls;
		
		String targetName = a.getValue("target", "bridews-server", 0);
		String serviceName = a.getValue("service", "BridgeWsServerService", 0);
		
		boolean remove = MCast.toboolean(a.getValue("remove", 0), false);
		boolean add    = MCast.toboolean(a.getValue("add", 0), false);
		boolean read   = MCast.toboolean(a.getValue("read", 0), true);
		int rounds = MCast.toint(a.getValue("rounds", 0), 10000);
		String token = a.getValue("token", 0);
		
		// load a client and configure it
		JwsStandaloneClient client = JwsStandaloneClient.instance();

		for (String url : urls)
			client.createTarget(url);
		
		// Load a service
		// 1.) get The Target
		// 2.) get create a new connection
		// 3.) get the provided service
		
		Target target = client.getTarget(targetName);
		Connection connection = target.createConnection();
		

		WSService ws = connection.getService(serviceName, WSService.class);
		
		if (token != null) {
			org.apache.cxf.endpoint.Client cxfClient = ClientProxy.getClient(ws);
			cxfClient.getOutInterceptors().add(new TokenOutInterceptor(token));
		}
		
		System.out.println("Connected to: " + target.getUrl() + " Service: " + serviceName);
		
		// The same code as in ws-client ...
		
		int cnt = 0;
		
		MStopWatch watch = new MStopWatch();
		watch.start();
		for (int i = 0; i < rounds; i++) {
			if (remove || read) {
				for (WSEntity entry : ws.getAll()) {
					//System.out.println("Entry: " + entry.getName());
					cnt++;
					System.out.print(".");
					if (cnt % 100 == 0)
						System.out.println(" " + cnt + " " + watch.getCurrentSeconds() );
					if (remove) ws.removeEntity(entry);
				}
			}
			if (add) {
				cnt++;
				ws.addEntity(new WSEntity( "auto_" + UUID.randomUUID().toString()));
			}
		}	
		watch.stop();
		System.out.println();
		System.out.println(watch.getCurrentTimeAsString());
		System.out.println(cnt + " Read / Write " + ((double)watch.getCurrentSeconds() / (double)cnt));

		
	}
	
}
