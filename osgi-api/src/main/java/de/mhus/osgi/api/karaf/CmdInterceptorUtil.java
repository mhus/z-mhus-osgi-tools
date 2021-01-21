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

import java.util.LinkedList;
import java.util.List;

import org.apache.karaf.shell.api.console.Session;

public class CmdInterceptorUtil {

    public static final String SESSION_KEY = "_de.mhus.osgi.api.karaf.CmdInterceptors";

    //    public void addInterceptor(Session session, CmdInterceptor interceptor) {
    //        @SuppressWarnings("unchecked")
    //        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(SESSION_KEY);
    //        if (interceptors == null) {
    //            interceptors = new LinkedList<>();
    //            session.put(SESSION_KEY, interceptors);
    //        }
    //        interceptors.add(interceptor);
    //    }

    public static void setInterceptor(Session session, CmdInterceptor interceptor) {
        @SuppressWarnings("unchecked")
        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(SESSION_KEY);
        if (interceptors == null) {
            interceptors = new LinkedList<>();
            session.put(SESSION_KEY, interceptors);
        } else {
            for (CmdInterceptor inter : interceptors) {
                if (inter.getClass()
                        .getCanonicalName()
                        .equals(interceptor.getClass().getCanonicalName())) {
                    interceptors.remove(inter);
                    try {
                        inter.onCmdEnd(session);
                    } catch (Throwable t) {
                    }
                }
            }
        }
        interceptors.add(interceptor);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CmdInterceptor> T getInterceptor(Session session, Class<?> clazz) {
        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(SESSION_KEY);
        if (interceptors == null) return null;
        for (CmdInterceptor interceptor : interceptors) {
            if (interceptor.getClass().getCanonicalName().equals(clazz.getCanonicalName()))
                return (T) interceptor;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends CmdInterceptor> T removeInterceptor(Session session, Class<?> clazz) {
        List<CmdInterceptor> interceptors = (List<CmdInterceptor>) session.get(SESSION_KEY);
        if (interceptors == null) return null;
        for (CmdInterceptor interceptor : interceptors) {
            if (interceptor.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
                interceptors.remove(interceptor);
                try {
                    interceptor.onCmdEnd(session);
                } catch (Throwable t) {
                }
                return (T) interceptor;
            }
        }
        return null;
    }
}
