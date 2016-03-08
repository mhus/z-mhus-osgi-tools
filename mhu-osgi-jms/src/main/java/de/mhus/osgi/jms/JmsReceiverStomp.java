package de.mhus.osgi.jms;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;

public class JmsReceiverStomp implements JmsReceiver {

	private String user;
	private String password;
	private String url;
	private boolean topic;
	private String queue;
	private JmsReceiverAdmin admin;
	private StompConnection connection;
	private Timer timer;

	public JmsReceiverStomp(String user, String password, String url, boolean topic, String queue) {
		this.user = user;
		this.password = password;
		this.url = url;
		this.topic = topic;
		this.queue = queue;
	}

	public void init(JmsReceiverAdmin admin) {
		this.admin = admin;

		try {
			connection = new StompConnection();
			URL u = new URL(url);
			connection.open(u.getHost(), u.getPort());
			         
			connection.connect(user, password);
			
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					doReceive();
				}
			}, 1000, 100);
			
			connection.subscribe((topic ? "/topic/" : "/queue/") + queue, Subscribe.AckModeValues.CLIENT);
//			connection.subscribe((topic ? "/topic/" : "/queue/") + queue, Subscribe.AckModeValues.AUTO);
			
			System.out.println("--- " + getName() + " Listening");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected void doReceive() {
		try {
			while (true) {
				StompFrame msg = connection.receive(1000);
				if (msg == null) return;
				System.out.println("--- " + getName() + " Received: " + msg.getBody());
				connection.ack(msg);
			}			
		} catch (SocketTimeoutException e) {
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void close() {

		try {
			timer.cancel();
			connection.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("--- " + getName() + " Closed");
		
	}

	public String getName() {
		return "stomp:/" + (topic ? "topic/" : "queue/") + queue;
	}

}
