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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "jms", name = "connection-remove", description = "Remove connection")
@Service
public class CmdConnectionRemove implements Action {

	@Argument(index=0, name="name", required=true, description="ID of the connection", multiValued=false)
    String name;
	
//    @Option(name = "-o", aliases = { "--online" }, description = "Create the datasource online and not a blueprint", required = false, multiValued = false)
//    boolean online;
	
	@Override
	public Object execute() throws Exception {
	
//		if (online) {
//			JmsManagerService service = JmsUtil.getService();
//			if (service == null) {
//				System.out.println("Service not found");
//				return null;
//			}
//			
//			JmsConnection con = service.getConnection(name);
//			if (con == null) {
//				System.out.println("Connection not found");
//				return null;
//			}
//			
//			service.removeConnection(name);
//			System.out.println("OK");
//		} else {
	        File karafBase = new File(System.getProperty("karaf.base"));
	        File deployFolder = new File(karafBase, "deploy");
	        File outFile = new File(deployFolder, "jms-openwire_" + name + ".xml");
	        if (outFile.exists()) {
	        	outFile.delete();
	        } else {
	        	System.out.println("File not found " + outFile.getAbsolutePath());
	        }
//		}
		return null;
	}

}
