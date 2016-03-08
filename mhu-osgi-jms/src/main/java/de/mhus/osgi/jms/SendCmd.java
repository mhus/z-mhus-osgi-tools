package de.mhus.osgi.jms;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.shell.commands.Action;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MStopWatch;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.jms.ClientJms;
import de.mhus.lib.jms.JmsConnection;
import de.mhus.lib.jms.JmsDestination;
import de.mhus.lib.karaf.jms.JmsUtil;

/*
MESSAGE
destination:/queue/a
message-id: <message-identifier>

hello queue a^@
            	String m = msg + " " + i;
            	String frame = "MESSAGE\n" + "destination:/topic/" + queue + "\nmessage-id:" + UUID.randomUUID() + "\n\n" + m + Stomp.NULL;

 */

@Command(scope = "jms", name = "direct-send", description = "send")
public class SendCmd implements Action {

    private static final Boolean NON_TRANSACTED = false;
    private static final long DELAY = 100;
    private static Log log = Log.getLog(SendCmd.class);
    
	@Argument(index=0, name="url", required=true, description="url tcp:// or name of the connection", multiValued=false)
    String url;
	
	@Argument(index=1, name="queue", required=true, description="...", multiValued=false)
    String queue;

	@Argument(index=2, name="msg", required=false, description="...", multiValued=false)
    String msg;

	@Option(name="-c", aliases="--count", required=false, description="...", multiValued=false)
    int count = 1;
	
	@Option(name="-t", aliases="--topic", description="Use a topic instead of a queue",required=false)
	boolean topic = false;
	
	@Option(name="-s", aliases="--synchronized", description="Create a temporary answer queue and wait for the answer",required=false)
	boolean sync = false;
	
	@Option(name="-u", aliases="--user", description="User",required=false)
	String user = "admin";
	
	@Option(name="-p", aliases="--password", description="Password",required=false)
	String password = "password";
	
	@Option(name="-h", aliases="--header", description="Header key=value",required=false,multiValued=true)
	String[] header;
	
	@Option(name="-m", aliases="--map", description="Map value key=value (Map Message)",required=false,multiValued=true)
	String[] map;
	
	@Option(name="-f", aliases="--file", description="File (Bytes Message)",required=false)
	String file;
	
	@Option(name="-o", aliases="--object", description="File (Object Message)",required=false)
	String object;
	
	boolean ownConnection = false;
	
	public Object execute(CommandSession s) throws Exception {

        JmsConnection connection = null;
        
        try {

        	if (url.indexOf(':') > 0 ) {
        		ownConnection = true;
	            connection = new JmsConnection(url, user, password);
        	} else {
        		connection = JmsUtil.getConnection(url);
        	}
        	
        	JmsDestination destination = topic ? connection.createTopic(queue) : connection.createQueue(queue);
        	destination.open();
        	
        	ClientJms client = new ClientJms(destination);
            client.setTimeout(30 * 1000);
            MStopWatch watch = new MStopWatch().start();
            for (int i = 0; i < count; i++) {
            	Message message = null;
            	
            	if (map != null) {
            		MapMessage mm = connection.getSession().createMapMessage();
                	for (String h : map) {
                		String name = MString.beforeIndex(h, '=');
                		String value = MString.afterIndex(h, '=');
                		if (MString.isSet(name))
                			mm.setString( name, value);
                	}
            		message = mm;
            	} else
            	if (file != null) {
            		BytesMessage mm = connection.getSession().createBytesMessage();
            		File f = new File(file);
                    byte[] b = MFile.readBinaryFile(f);
                    mm.writeBytes(b);
            	} else
            	if (object != null) {
            		Object o = s.get(object);
            		message = connection.getSession().createObjectMessage((Serializable) o);
            	} else
            		message = connection.getSession().createTextMessage(msg);
            	
                //String id = UUID.randomUUID().toString();
                
                if (header != null) {
                	for (String h : header) {
                		String name = MString.beforeIndex(h, '=');
                		String value = MString.afterIndex(h, '=');
                		if (MString.isSet(name))
                			message.setStringProperty( name, value);
                	}
                }
                
                log.i("Sending message #" + i, message);

                if (sync) {
                	Message res = client.sendJms(message);
                	if (res != null) {
                		if (res instanceof MapMessage)
                			((MapMessage)res).getMapNames(); // touch the map message
                	}
            		log.i("Answer", watch.getCurrentTimeAsString(true), res);
                } else
                	client.sendJmsOneWay(message);

            }
            watch.stop();
            log.i(watch.getCurrentTimeAsString(true));
            // tell the subscribers we're done
//            producer.send(session.createTextMessage("END"));

        } catch (Exception e) {
            log.e("Caught exception!", e);
        }
        finally {
            if (connection != null && ownConnection) {
                try {
                    connection.close();
                } catch (Exception e) {
                    log.e("Could not close an open connection...",e);
                }
            }
        }	  
		
		return null;
	}
	
}
