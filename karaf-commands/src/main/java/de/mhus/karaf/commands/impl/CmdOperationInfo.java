/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.impl;

import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.core.M;
import de.mhus.lib.core.operation.Operation;
import de.mhus.lib.core.operation.OperationDescription;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.services.OperationManager;

@Command(scope = "mhus", name = "operation-info", description = "Show operation information")
@Service
public class CmdOperationInfo extends AbstractCmd {

    @Argument(
            index = 0,
            name = "id/pathVersion",
            required = true,
            description = "Ident of the operation",
            multiValued = false)
    String name;

    @Override
    public Object execute2() throws Exception {

        OperationManager api = M.l(OperationManager.class);
        if (api == null) {
            System.out.println("OperationManager not found");
            return null;
        }
        Operation oper = api.getOperation(name);
        if (oper == null) {
            System.out.println("Operation not found");
            return null;
        }
        OperationDescription desc = oper.getDescription();
        if (desc == null) System.out.println("Operation has no description");
        else {
            System.out.println("Name      : " + desc.getPathVersion());
            System.out.println("Path      : " + desc.getPath());
            System.out.println("Version   : " + desc.getVersion());
            System.out.println("Caption   : " + desc.getCaption());
            System.out.println("Title     : " + desc.getTitle());
            System.out.println("Id        : " + desc.getUuid());
            System.out.println("Parameters: " + desc.getParameterDefinitions());
            System.out.println("Form      : " + desc.getForm());
            System.out.println("Labels:");
            for (Entry<String, Object> label : desc.getLabels().entrySet())
                System.out.println("  " + label.getKey() + "=" + label.getValue());
        }
        System.out.println("Class     : " + oper.getClass().getCanonicalName());
        System.out.println(
                "Bundle    : " + MOsgi.getBundleCaption(FrameworkUtil.getBundle(oper.getClass())));

        return oper;
    }
}
