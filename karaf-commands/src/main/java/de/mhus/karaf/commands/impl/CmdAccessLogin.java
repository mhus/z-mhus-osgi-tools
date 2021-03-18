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

import java.util.Locale;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.M;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.aaa.AccessApi;
import de.mhus.lib.core.aaa.SubjectEnvironment;
import de.mhus.lib.core.console.Console;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.karaf.CmdInterceptor;
import de.mhus.osgi.api.karaf.CmdInterceptorUtil;

@Command(
        scope = "mhus",
        name = "access-login",
        description = "Access Control - Login user an bind session to console")
@Service
public class CmdAccessLogin extends AbstractCmd {

    @Reference Session session;

    @Argument(
            index = 0,
            name = "user",
            required = true,
            description = "User id to login",
            multiValued = false)
    String user;

    @Argument(
            index = 1,
            name = "password",
            required = false,
            description = "Password of the user, if not set the password will be prompted",
            multiValued = false)
    String pass;

    @Option(
            name = "-f",
            aliases = "--force",
            required = false,
            description = "Do not ask for a password",
            multiValued = false)
    boolean force;

    @Override
    public Object execute2() throws Exception {

        if (force) {
            Subject subject = Aaa.createSubjectWithoutCheck(user);
            CmdInterceptorUtil.setInterceptor(session, new AaaInterceptor(subject));
        } else {
            if (pass == null) pass = Console.get().readPassword();
            Subject subject = M.l(AccessApi.class).createSubject();
            if (!Aaa.login(subject, user, pass, true, Locale.getDefault())) {
                System.out.println("Login failed");
                return null;
            }
            CmdInterceptorUtil.setInterceptor(session, new AaaInterceptor(subject));
        }
        System.out.println("OK");

        return null;
    }

    public static class AaaInterceptor implements CmdInterceptor {

        private Subject subject;
        private SubjectEnvironment env;

        public AaaInterceptor(Subject subject) {
            this.subject = subject;
        }

        @Override
        public void onCmdStart(Session session) {
            if (subject != null) env = Aaa.asSubject(subject);
        }

        @Override
        public void onCmdEnd(Session session) {
            if (env != null) env.close();
            env = null;
        }
    }
}
