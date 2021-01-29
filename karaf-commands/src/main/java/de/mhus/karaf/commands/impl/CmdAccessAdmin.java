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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.M;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.shiro.AccessApi;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.osgi.api.karaf.AbstractCmd;
import de.mhus.osgi.api.karaf.CmdInterceptorUtil;

@Command(
        scope = "mhus",
        name = "access-admin",
        description = "Access Control - try to login as admin and bind session to console")
@Service
public class CmdAccessAdmin extends AbstractCmd {

    @Reference Session session;

    @Override
    public Object execute2() throws Exception {

        String user = "admin";
        String pass = MApi.get().getCfgString(AccessApi.class, "adminPassword", "secret");
        Subject subject = M.l(AccessApi.class).createSubject();
        AccessUtil.login(subject, user, pass, true, Locale.getDefault());
        CmdInterceptorUtil.setInterceptor(session, new CmdAccessLogin.AaaInterceptor(subject));
        System.out.println("OK");

        return null;
    }
}
