/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.BackingEngineFactory;

import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.api.services.MOsgi;

public class KarafJaasUtil {

    /**
     * Return true if the user is member of the group.
     *
     * @param realmName
     * @param userName
     * @param groupName
     * @return true if user is member of group
     * @throws NotFoundException
     */
    public static boolean isMemberOf(String realmName, String userName, String groupName)
            throws NotFoundException {
        UserPrincipal user = getUser(realmName, userName);
        List<GroupPrincipal> groups = getGroupsForUser(realmName, user);
        for (GroupPrincipal group : groups) if (group.getName().equals(groupName)) return true;
        return false;
    }

    public static UserPrincipal getUser(String realmName, String userName)
            throws NotFoundException {
        JaasRealm realm = getRealm(realmName);
        if (realm == null) throw new NotFoundException("realm not found", realmName);
        AppConfigurationEntry entry = null;
        for (AppConfigurationEntry appConfig : realm.getEntries()) {
            entry = appConfig; // user first one
            break;
        }

        BackingEngine engine = getBackingEngine(entry);
        for (UserPrincipal user : engine.listUsers()) {
            if (user.getName().equals(userName)) return user;
        }
        throw new NotFoundException("user not found", realmName, userName);
    }

    public static GroupPrincipal getGroup(String realmName, String groupName)
            throws NotFoundException {
        JaasRealm realm = getRealm(realmName);
        if (realm == null) throw new NotFoundException("realm not found", realmName);
        AppConfigurationEntry entry = null;
        for (AppConfigurationEntry appConfig : realm.getEntries()) {
            entry = appConfig; // user first one
            break;
        }

        BackingEngine engine = getBackingEngine(entry);
        for (GroupPrincipal group : engine.listGroups().keySet())
            if (group.getName().equals(groupName)) return group;
        throw new NotFoundException("group not found", realmName, groupName);
    }

    public static List<GroupPrincipal> getGroupsForUser(String realmName, UserPrincipal user)
            throws NotFoundException {
        JaasRealm realm = getRealm(realmName);
        if (realm == null) throw new NotFoundException("realm not found", realmName);
        AppConfigurationEntry entry = null;
        for (AppConfigurationEntry appConfig : realm.getEntries()) {
            entry = appConfig; // user first one
            break;
        }

        BackingEngine engine = getBackingEngine(entry);
        return engine.listGroups(user);
    }

    public static BackingEngine getBackingEngine(AppConfigurationEntry entry) {

        List<BackingEngineFactory> engineFactories =
                MOsgi.getServices(BackingEngineFactory.class, null);

        for (BackingEngineFactory factory : engineFactories) {
            String loginModuleClass =
                    (String) entry.getOptions().get(ProxyLoginModule.PROPERTY_MODULE);
            if (factory.getModuleClass().equals(loginModuleClass)) {
                return factory.build(entry.getOptions());
            }
        }
        return null;
    }

    public static List<JaasRealm> getRealms() {
        return getRealms(false);
    }

    public static List<JaasRealm> getRealms(boolean hidden) {

        List<JaasRealm> realms = MOsgi.getServices(JaasRealm.class, null);

        if (hidden) {
            return realms;
        } else {
            Map<String, JaasRealm> map = new TreeMap<>();
            for (JaasRealm realm : realms) {
                if (!map.containsKey(realm.getName())
                        || realm.getRank() > map.get(realm.getName()).getRank()) {
                    map.put(realm.getName(), realm);
                }
            }
            return new ArrayList<>(map.values());
        }
    }

    public static JaasRealm getRealm(String name) {

        List<JaasRealm> realms = MOsgi.getServices(JaasRealm.class, null);
        JaasRealm out = null;
        for (JaasRealm realm : realms)
            if (realm.getName().equals(name) && (out == null || realm.getRank() > out.getRank()))
                out = realm;
        return out;
    }
}
