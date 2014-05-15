package de.hfo.test.ws.ws_client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.xml.ws.Service;
import javax.xml.namespace.QName;

import de.hfo.test.ws.ws_model.WSEntity;
import de.hfo.test.ws.ws_model.WSService;
import de.mhus.lib.core.MStopWatch;

@SuppressWarnings("restriction")
public class WSClientMain {

	public static void main(String[] args) throws MalformedURLException {

		URL url = new URL("http://localhost:8181/cxf/hehe?wsdl");
		QName qname = new QName("http://impl.ws_server.ws.test.hfo.de/", "WSServiceImplService");
		
		Service service = Service.create(url, qname);
		
		WSService ws = service.getPort(WSService.class);
		int cnt = 0;
		
		MStopWatch watch = new MStopWatch();
		watch.start();
		for (int i = 0; i < 10000; i++) {
			for (WSEntity entry : ws.getAll()) {
				//System.out.println("Entry: " + entry.getName());
				cnt++;
				System.out.print(".");
				if (cnt % 100 == 0)
					System.out.println(" " + cnt + " " + watch.getCurrentSeconds() );
				//ws.remvoeEntity(entry);
			}
		}	
		watch.stop();
		System.out.println();
		System.out.println(watch.getCurrentTimeAsString(true));
		System.out.println(cnt + " Read / Write " + ((double)watch.getCurrentSeconds() / (double)cnt));
		
		//ws.addEntity(new WSEntity( "auto_" + UUID.randomUUID().toString()));
		
	}

}
