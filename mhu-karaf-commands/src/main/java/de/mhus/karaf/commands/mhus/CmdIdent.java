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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.service.ServerIdent;

@Command(scope = "mhus", name = "ident", description = "Print the Server Ident")
@Service
public class CmdIdent implements Action {

	@Option(name="-a", aliases="--all", description="Print also hostname and pid",required=false)
	private boolean full;

    @Option(name="-p", aliases="--print", description="Print also attributes",required=false)
    private boolean attr;
    
	@Override
	public Object execute() throws Exception {
		ServerIdent service = M.l(ServerIdent.class);
		if (attr)
		    System.out.println(ServerIdent.getAttributes());
		return service.toString() + (full ? " " + MSystem.getPid() + "@" + MSystem.getHostname() : "");
	}

}
