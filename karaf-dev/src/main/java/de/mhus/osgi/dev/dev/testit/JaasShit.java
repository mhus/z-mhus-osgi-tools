/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
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
package de.mhus.osgi.dev.dev.testit;

import java.security.Principal;
import java.util.List;
import java.util.Map.Entry;

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;

import de.mhus.lib.core.util.EnumerationIterator;
import de.mhus.osgi.api.util.KarafJaasUtil;

public class JaasShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println(
                " user realmname username\n" + " realm realmname\n" + " group realmname groupname");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        if (cmd.equals("realm")) {
            JaasRealm realm = KarafJaasUtil.getRealm(parameters[0]);
            System.out.println(realm);
            if (realm != null) {
                System.out.println("Name: " + realm.getName());
                System.out.println("Rank: " + realm.getRank());
                System.out.println("Class: " + realm.getClass());
                for (AppConfigurationEntry entry : realm.getEntries()) {
                    System.out.println(">>> Entry: " + entry.getLoginModuleName());
                    System.out.println("  Class: " + entry.getClass());
                    System.out.println("  Flags: " + entry.getControlFlag());
                    System.out.println("  Options:");
                    for (Entry<String, ?> option : entry.getOptions().entrySet())
                        System.out.println("    " + option.getKey() + " = " + option.getValue());
                    System.out.println("<<<");
                }
            }
            return null;
        }
        if (cmd.equals("user")) {
            UserPrincipal user = KarafJaasUtil.getUser(parameters[0], parameters[1]);
            System.out.println(user);

            List<GroupPrincipal> groups = KarafJaasUtil.getGroupsForUser(parameters[0], user);
            for (GroupPrincipal group : groups) {
                System.out.println("  Group: " + group.getName());
            }
            return null;
        }
        if (cmd.equals("group")) {
            GroupPrincipal group = KarafJaasUtil.getGroup(parameters[0], parameters[1]);
            System.out.println("Group: " + group.getName());
            System.out.println("Members:");
            for (Principal member : new EnumerationIterator<Principal>(group.members())) {
                System.out.println("  " + member.getName());
            }
        }
        return null;
    }
}
