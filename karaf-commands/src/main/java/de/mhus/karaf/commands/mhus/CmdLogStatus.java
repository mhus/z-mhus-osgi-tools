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
package de.mhus.karaf.commands.mhus;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.mapi.IApi;
import de.mhus.lib.mutable.KarafMApiImpl;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "log-status", description = "Print log engine status")
@Service
public class CmdLogStatus extends AbstractCmd {

    @Override
    public Object execute2() throws Exception {

        IApi s = MApi.get();
        if (!(s instanceof KarafMApiImpl)) {
            System.out.println("Karaf MApi not set");
            return null;
        }
        KarafMApiImpl api = (KarafMApiImpl) s;

        System.out.println("Default Level  : " + api.getLogFactory().getDefaultLevel());
        System.out.println("Trace          : " + api.isFullTrace());
        System.out.println("LogFoctory     : " + api.getLogFactory().getClass().getSimpleName());
        System.out.println("MaxMsgSize     : " + Log.getMaxMsgSize());
        System.out.println("DirtyTrace     : " + MApi.isDirtyTrace());
        if (api.getLogFactory().getParameterMapper() != null)
            System.out.println(
                    "ParameterMapper: "
                            + api.getLogFactory().getParameterMapper().getClass().getSimpleName());

        for (String name : api.getTraceNames()) System.out.println(name);

        return null;
    }
}
