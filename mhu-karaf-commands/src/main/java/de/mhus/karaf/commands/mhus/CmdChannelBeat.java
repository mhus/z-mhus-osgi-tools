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

import de.mhus.osgi.api.jms.JmsManagerService;
import de.mhus.osgi.api.jms.JmsUtil;

@Command(scope = "jms", name = "channel-beat", description = "Reconnect channel")
@Service
public class CmdChannelBeat implements Action {

	@Argument(index=0, name="name", required=false, description="ID of the channel", multiValued=false)
    String name;

	@Override
	public Object execute() throws Exception {

		JmsManagerService service = JmsUtil.getService();
		if (service == null) {
			System.out.println("Service not found");
			return null;
		}

		if (name == null || name.equals("*")) {
			service.doChannelBeat();
		} else {
			if (service.getChannel(name).getChannel() == null)
				service.getChannel(name).reset();
			service.getChannel(name).getChannel().doBeat();
		}
		System.out.println("OK");
		return null;
	}

}
