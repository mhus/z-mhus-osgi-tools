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
package de.mhus.osgi.vaadinkarafbridge.impl;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.vaadinbridge.VaadinConfigurableResourceProviderAdmin;

@Command(scope = "vaadin", name = "resourceAdd", description = "Add a resource provider")
@Service
public class CmdVaadinResourceAdd implements Action {

	@Argument(index=0, name="bundle", required=true, description="Bundle Name", multiValued=false)
    String bundle;

	@Argument(index=1, name="pathes", required=true, description="Pathes", multiValued=true)
    String[] pathes;

	@Reference
	private VaadinConfigurableResourceProviderAdmin provider;

	@Override
	public Object execute() throws Exception {
		
//		System.out.println("ADD: " + bundle + ":" + pathes);
		provider.addResource(bundle, pathes);
		
		return null;
	}

}
