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
package de.mhus.osgi.mail.karaf;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MString;
import de.mhus.osgi.mail.core.SendQueue;
import de.mhus.osgi.mail.core.SendQueueManager;
import de.mhus.osgi.mail.core.SmtpSendQueue;

@Command(scope = "mail", name = "registersmtp", description = "Register a new SMTP queue")
@Service
public class CmdRegisterSmtp implements Action {

	private SendQueueManager admin;

	@Argument(index=0, name="name", required=true, description="Queue Name", multiValued=false)
    String name;
	@Argument(index=1, name="property", required=true, description="properties key=value and file:<file> to load from", multiValued=true)
    String[] properties;

	@Override
	public Object execute() throws Exception {
		
		SendQueue q = admin.getQueue(name);
		if (q != null) {
			System.out.println("Queue already exists");
			return null;
		}
		
		Properties p = new Properties();
		for (String item : properties) {
			if (item.indexOf('=') > 1) {
				p.put(MString.beforeIndex(item, '='), MString.afterIndex(item, '='));
			} else 
			if (item.startsWith("file:")) {
				File f = new File(item.substring(5));
				if (f.exists() && f.isFile()) {
					FileInputStream fis = new FileInputStream(f);
					p.load(fis);
					fis.close();
				} else {
					System.out.println("Can't load file " + f.getName());
				}
			}
		}
		
		SmtpSendQueue smtp = new SmtpSendQueue(name, p);
		admin.registerQueue(smtp);

		System.out.println("OK");
		
		return null;
	}
	
	public void setAdmin(SendQueueManager admin) {
		this.admin = admin;
	}

}
