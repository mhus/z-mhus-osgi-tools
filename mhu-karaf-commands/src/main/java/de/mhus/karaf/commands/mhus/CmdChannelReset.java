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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.api.jms.JmsDataChannel;
import de.mhus.osgi.api.jms.JmsManagerService;
import de.mhus.osgi.api.jms.JmsUtil;

@Command(scope = "jms", name = "channel-reset", description = "Reset channels")
@Service
public class CmdChannelReset implements Action {

	@Argument(index=0, name="name", required=true, description="ID of the channel or * for all", multiValued=false)
    String name;

	@Override
	public Object execute() throws Exception {

		JmsManagerService service = JmsUtil.getService();
		if (service == null) {
			System.out.println("Service not found");
			return null;
		}

		if (name == null || name.equals("*"))
			for (JmsDataChannel c : service.getChannels()) {
				try {
					System.out.println(c);
					c.reset();
					if (c.getChannel() != null) {
						c.getChannel().reset();
						c.getChannel().open();
					}else
						System.out.println("... channel is null");
				} catch (Throwable t) {
					System.out.println(t);
				}
			}
		else {
			JmsDataChannel channel = service.getChannel(name);
			if (channel == null) {
				System.out.println("Channel not found");
				return null;
			}
			channel.reset();
			channel.getChannel().reset();
			channel.getChannel().open();
		}
		System.out.println("OK");
		return null;
	}

}
