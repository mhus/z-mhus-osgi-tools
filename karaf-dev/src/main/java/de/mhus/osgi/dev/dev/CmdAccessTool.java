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
package de.mhus.osgi.dev.dev;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.LifecycleUtils;

import de.mhus.lib.core.M;
import de.mhus.lib.core.aaa.Aaa;
import de.mhus.lib.core.aaa.AccessApi;
import de.mhus.lib.core.aaa.SubjectEnvironment;
import de.mhus.lib.core.aaa.TrustedToken;
import de.mhus.lib.core.util.Value;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.aaa.RealmServiceProvider;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(
        scope = "mhus",
        name = "access-tool",
        description = "Access Control - tool")
@Service
public class CmdAccessTool extends AbstractCmd {

    @Argument(
            index = 0,
            name = "cmd",
            required = true,
            description =
                    "Command to execute\n"
                    + " role <account> <role> - check if user has role\n"
                    + " access <account> <perm> - check if user has permission\n"
                    + " roles <account> - rint all roles for the account\n"
                    + " perms <account> - print all perms for the user\n"
                    + " info - print informations about AAA\n"
                    + " admininfo - print infos about the admin user\n"
                    + " guestinfo - print infos about the guest user\n"
                    + " resetrealms\n"
                    + " reloadrealms\n"
                    + " login <user> <pass> - test login as user",
            multiValued = false)
    String cmd;

    @Argument(
            index = 1,
            name = "paramteters",
            required = false,
            description = "Parameters",
            multiValued = true)
    String[] parameters;

    @Override
    public Object execute2() throws Exception {

        if (cmd.equals("admininfo")) {
            SimpleAccount info = Aaa.ADMIN;
            System.out.println("Account: " + info);
            System.out.println("Perms  : " +  info.getObjectPermissions() );
            System.out.println("Perms  : " +  info.getStringPermissions() );
            System.out.println("Roles  : " +  info.getRoles() );
        } else
        if (cmd.equals("guestinfo")) {
            SimpleAccount info = Aaa.GUEST;
            System.out.println("Account: " + info);
            System.out.println("Perms  : " +  info.getObjectPermissions() );
            System.out.println("Perms  : " +  info.getStringPermissions() );
            System.out.println("Roles  : " +  info.getRoles() );
        } else
        if (cmd.equals("role")) {
            Subject subject = Aaa.createSubjectWithoutCheck(parameters[0]);
            try (SubjectEnvironment access = Aaa.asSubject(subject)) {
                System.out.println(Aaa.getPrincipal());
                boolean res = subject.hasRole(parameters[1]);
                System.out.println("Role: " + res );
            }
        } else
        if (cmd.equals("access")) {
            Subject subject = Aaa.createSubjectWithoutCheck(parameters[0]);
            try (SubjectEnvironment access = Aaa.asSubject(subject)) {
                System.out.println(Aaa.getPrincipal());
                boolean res = subject.isPermitted(parameters[1]);
                System.out.println("Permission: " + res );
            }
        } else
        if (cmd.equals("roles")) {

            Collection<Realm> realms = ((DefaultSecurityManager)SecurityUtils.getSecurityManager()).getRealms();
            for (Realm realm : realms) {
                System.out.println("Realm  : " + realm);
                try {
                    AuthenticationInfo info = realm.getAuthenticationInfo(new TrustedToken(parameters[0]));
                    if (info != null && info instanceof SimpleAccount) {
                        System.out.println("Account: " + info);
                        System.out.println("Roles  : " +  ((SimpleAccount)info).getRoles() );
                        return null;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else
        if (cmd.equals("perms")) {

            Collection<Realm> realms = ((DefaultSecurityManager)SecurityUtils.getSecurityManager()).getRealms();
            for (Realm realm : realms) {
                System.out.println("Realm  : " + realm);
                try {
                    AuthenticationInfo info = realm.getAuthenticationInfo(new TrustedToken(parameters[0]));
                    if (info != null && info instanceof SimpleAccount) {
                        System.out.println("Account: " + info);
                        System.out.println("Perms  : " +  ((SimpleAccount)info).getObjectPermissions() );
                        System.out.println("Perms  : " +  ((SimpleAccount)info).getStringPermissions() );
                        return null;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else
        if (cmd.equals("info")) {
            AccessApi api = M.l(AccessApi.class);
            System.out.println("API: " + api.getClass().getCanonicalName());
            SecurityManager manager = api.getSecurityManager();
            System.out.println("Manager: " + manager.getClass().getCanonicalName());
            String principal = Aaa.getPrincipal();
            Value<SimpleAccount> info = new Value<>();
            if (manager instanceof DefaultSecurityManager) {
                DefaultSecurityManager def = (DefaultSecurityManager)manager;
                System.out.println("Realms:");
                def.getRealms().forEach(r -> {
                    System.out.println("  " + r.getName() + " " + r.getClass().getCanonicalName());
                    AuthenticationInfo i = r.getAuthenticationInfo(new TrustedToken(principal) );
                    if (i instanceof SimpleAccount)
                        info.value = (SimpleAccount) i;
                });
                //TODO more ...
            }
            System.out.println("Account: " + info.value);
            if (info != null) {
                System.out.println("Perms  : " +  info.value.getObjectPermissions() );
                System.out.println("Perms  : " +  info.value.getStringPermissions() );
                System.out.println("Roles  : " +  info.value.getRoles() );
            }    
        } else
        if (cmd.equals("resetrealms")) {
            List<Realm> realms = new ArrayList<>();
            for (RealmServiceProvider service : MOsgi.getServices(RealmServiceProvider.class, null)) {
                System.out.println("Add Realm " + service.getService().getName() + " " + service.getService().getClass().getCanonicalName());
                realms.add(service.getService());
            }
            LifecycleUtils.init(realms);
            DefaultSecurityManager manager = new DefaultSecurityManager(realms);
            SecurityUtils.setSecurityManager(manager);
        } else
        if (cmd.equals("reloadrealms")) {
            List<Realm> realms = new ArrayList<>();
            for (RealmServiceProvider service : MOsgi.getServices(RealmServiceProvider.class, null)) {
                System.out.println("Add Realm " + service.getService().getName() + " " + service.getService().getClass().getCanonicalName());
                realms.add(service.getService());
            }
            LifecycleUtils.init(realms);
            ((DefaultSecurityManager)SecurityUtils.getSecurityManager()).setRealms(realms);
        } else
        if (cmd.equals("login")) {
            System.out.println(Aaa.getPrincipal());
            String ticket = Aaa.createAccountTicket(parameters[0], parameters[1]);
            Subject subject = Aaa.login(ticket);
            try (SubjectEnvironment access = Aaa.asSubject(subject)) {
                System.out.println(Aaa.getPrincipal());
                System.out.println(Aaa.getPrincipalData());
            }
            System.out.println(Aaa.getPrincipal());
        }
        return null;
    }
}
