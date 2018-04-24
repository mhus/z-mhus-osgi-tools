/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.mhus;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.services.util.TemplateUtils;

@Command(scope = "jms", name = "channel-add", description = "add channel")
@Service
public class CmdChannelAdd implements Action {

	@Argument(index=0, name="name", required=true, description="ID of the channel", multiValued=false)
    String name;

	@Argument(index=1, name="connection", required=true, description="ID of the connection", multiValued=false)
    String connection;

	@Argument(index=2, name="destination", required=true, description="Name of the destination queue or topic", multiValued=false)
    String destination;

	@Argument(index=3, name="interface", required=true, description="Interface or Implementation", multiValued=false)
    String ifc;

//    @Option(name = "-o", aliases = { "--online" }, description = "Create the datasource online and not a blueprint", required = false, multiValued = false)
//    boolean online;
	
    @Option(name = "-t", aliases = { "--topic" }, description = "Destinantion is a topic", required = false, multiValued = false)
    boolean topic;

    @Option(name = "-s", aliases = { "--service" }, description = "Channel is a service, not a client", required = false, multiValued = false)
    boolean service;
    
	@Override
	public Object execute() throws Exception {

//		if (online) {
//			
//			JmsManagerService service = JmsUtil.getService();
//			if (service == null) {
//				System.out.println("Service not found");
//				return null;
//			}
//
//			JmsDataChannelImpl impl = new JmsDataChannelImpl();
//			impl.setName(name);
//			impl.setConnectionName(connection);
//			impl.setDestination(destination);
//			impl.setDestinationTopic(topic);
//			if (this.service)
//				impl.setImplementation(ifc);
//			else
//				impl.setIfc(ifc);
//			
//			service.addChannel(impl);
//			
//		} else {
			
	        File karafBase = new File(System.getProperty("karaf.base"));
	        File deployFolder = new File(karafBase, "deploy");
	        File outFile = new File(deployFolder, "jms-channel_" + name + ".xml");

	        HashMap<String, String> properties = new HashMap<String, String>();
	        properties.put("connection", connection);
	        properties.put("destination", destination);
	        properties.put("topic", String.valueOf(topic));
	        properties.put("interface", service ? "" : ifc);
	        properties.put("implementation", service ? ifc : "");
	        properties.put("name", name);
	        String templateFile = "jms-channel.xml";
            InputStream is = this.getClass().getResourceAsStream(templateFile);
            if (is == null) {
                throw new IllegalArgumentException("Template resource " + templateFile + " doesn't exist");
            }
            TemplateUtils.createFromTemplate(outFile, is, properties);

			
//		}
		
		return null;
	}

}
