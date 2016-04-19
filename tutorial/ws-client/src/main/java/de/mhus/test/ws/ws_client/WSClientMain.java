package de.mhus.test.ws.ws_client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.xml.ws.Service;
import javax.xml.namespace.QName;

import de.mhus.lib.core.MArgs;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MStopWatch;
import de.mhus.test.ws.ws_model.WSEntity;
import de.mhus.test.ws.ws_model.WSService;

@SuppressWarnings("restriction")
public class WSClientMain {

	public static void main(String[] args) throws MalformedURLException {

		MArgs a = new MArgs(args);
		String urlName = a.getValue("url", "http://localhost:8181/cxf/hehe?wsdl", 0 );
		String nameSpaceName = a.getValue("namespace", "http://impl.ws_server.ws.test.mhus.de/", 0);
		String serviceName = a.getValue("service", "WSServiceImplService", 0);
		
		boolean remove = MCast.toboolean(a.getValue("remove", 0), false);
		boolean add    = MCast.toboolean(a.getValue("add", 0), false);
		boolean read   = MCast.toboolean(a.getValue("read", 0), true);
		int rounds = MCast.toint(a.getValue("rounds", 0), 10000);
		
		URL url = new URL(urlName);
		QName qname = new QName(nameSpaceName, serviceName);
		
		Service service = Service.create(url, qname);
		
		WSService ws = service.getPort(WSService.class);
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
