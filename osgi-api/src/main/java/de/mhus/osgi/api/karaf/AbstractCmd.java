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
package de.mhus.osgi.api.karaf;

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.MString;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.util.MObject;
import io.opentracing.Scope;

public abstract class AbstractCmd extends MObject implements Action {

    @Option(
            name = "-tbl",
            aliases = {"--table"},
            description = "Console table options",
            required = false,
            multiValued = false)
    protected String tblOpt;

    @Option(
            name = "-ta",
            aliases = {"--table-all"},
            description = "Console table print all",
            required = false,
            multiValued = false)
    private boolean tableAll;

    @Option(
            name = "--trace",
            description = "Console table print all",
            required = false,
            multiValued = false)
    private String trace;

    @Reference private Session session;

    protected boolean cmdIsPermissionDependent = false;

    @Override
    public final Object execute() throws Exception {
        if (tableAll) {
            if (tblOpt == null) tblOpt = "all=1";
            else tblOpt = "all=1 " + tblOpt;
        }
        @SuppressWarnings("unchecked")
        List<CmdInterceptor> interceptors =
                (List<CmdInterceptor>) session.get(CmdInterceptorUtil.SESSION_KEY);
        if (interceptors != null) {
            try {
                for (CmdInterceptor interceptor : interceptors)
                    try {
                        interceptor.onCmdStart(session);
                    } catch (Throwable t) {
                        log().d(t);
                    }
            } catch (ClassCastException e) {
                log().d("Reset Interceptors");
                interceptors.clear();
            }
        }

        try {
            if (!Console.isInitialized()) { // init console by default
                ConsoleInterceptor interceptor = new ConsoleInterceptor(new KarafConsole(session));
                CmdInterceptorUtil.setInterceptor(session, interceptor);
                interceptor.onCmdStart(session);
            }
        } catch (Throwable t) {
            log().d(t);
        }

        Scope scope = null;
        if (MString.isSet(trace)) {
            scope = ITracer.get().start(getClass().getName(), trace);
        }
        // shorten thread name - for logging
        String tName = Thread.currentThread().getName();
        if (tName == null || tName.length() == 0) {
            tName = getClass().getName();
            Thread.currentThread().setName(tName); // should not happen
        } else if (tName.length() > 40) {
            tName = tName.substring(0, 40);
            Thread.currentThread().setName(tName);
        }
        if (tName.indexOf('\n') > -1) {
            tName = tName.replace('\n', ' ');
            Thread.currentThread().setName(tName);
        }
        if (cmdIsPermissionDependent && Aaa.getPrincipal() == null)
            System.out.println("--- Warn: You have guest privileges");
        // execute
        Object ret = null;
        try {
            ret = execute2();
        } finally {
            if (scope != null) {
                try {
                    scope.close();
                } catch (Throwable t) {
                }
            }
            if (interceptors != null) {
                for (CmdInterceptor interceptor : interceptors)
                    try {
                        interceptor.onCmdEnd(session);
                    } catch (Throwable t) {
                        log().d(t);
                    }
            }
        }
        return ret;
    }

    protected Session getSession() {
        return session;
    }

    public abstract Object execute2() throws Exception;
}
