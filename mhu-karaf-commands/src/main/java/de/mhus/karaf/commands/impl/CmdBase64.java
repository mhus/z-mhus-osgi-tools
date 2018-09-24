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
package de.mhus.karaf.commands.impl;

import java.util.Base64;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;

@Command(scope = "java", name = "base64", description = "Base64 encoding")
@Service
public class CmdBase64 implements Action {

	@Argument(index=0, name="cmd", required=true, description="Command:\n"
			+ " decode <base64 string>\n"
			+ " encode <Hex string: FF FF FF FF>", multiValued=false)
    String cmd;

	@Argument(index=1, name="paramteters", required=false, description="Parameters", multiValued=true)
    String[] parameters;

	@Override
	public Object execute() throws Exception {
		switch (cmd) {
		case "decode": {
			byte[] out = Base64.getDecoder().decode(parameters[0]);
			System.out.println(
					MString.toHexDump(out, 50)
					);
		} break;
		case "encode": {
			String[] parts = parameters[0].split(" ");
			byte[] data = new byte[parts.length];
			for (int i = 0; i < parts.length; i++)
				data[i] = (byte)MCast.tointFromHex(parts[i]);
			String out = Base64.getEncoder().encodeToString(data);
			System.out.println(out);
		} break;
		default:
			System.out.println("Command unknown");
		}
		return null;
	}

}
