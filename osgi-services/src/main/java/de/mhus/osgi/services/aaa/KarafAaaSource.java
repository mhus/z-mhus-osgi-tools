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
package de.mhus.osgi.services.aaa;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.BackingEngineFactory;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.LoginCallbackHandler;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.osgi.api.services.MOsgi;

public class KarafAaaSource implements AccountSource {

    private String realm = "karaf";
    BackingEngine engine = null;
    private String moduleName;

    @Override
    public Account findAccount(String account) {

        try {

            if (engine == null) {
                JaasRealm realmObj = null;
                for (JaasRealm r : MOsgi.getServices(JaasRealm.class, null))
                    if (r.getName().equals(realm)) {
                        realmObj = r;
                        break;
                    }
                AppConfigurationEntry[] entries = realmObj.getEntries();
                if (entries != null) {
                    for (AppConfigurationEntry e : entries) {
                        String moduleClass =
                                (String) e.getOptions().get(ProxyLoginModule.PROPERTY_MODULE);
                        if (moduleName == null
                                || e.getLoginModuleName().equals(moduleName)
                                || moduleName.equals(moduleClass)) {
                            engine = getBackingEngine(e);
                            if (engine != null) break;
                        }
                    }
                }
            }

            if (engine == null) {
                return null;
            }

            List<UserPrincipal> users = engine.listUsers();
            for (UserPrincipal user : users) {
                if (user.getName().equals(account)) {
                    return new KarafAccount(engine, user);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public BackingEngine getBackingEngine(AppConfigurationEntry entry) {
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

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
        engine = null;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    private class KarafAccount implements Account {

        private BackingEngine engine;
        private UserPrincipal user;
        private HashSet<String> groups = new HashSet<>();

        public KarafAccount(BackingEngine engine, UserPrincipal user) {
            this.engine = engine;
            this.user = user;
            reloadAccount();
        }

        @Override
        public String getName() {
            return user.getName();
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean validatePassword(String password) {

            try {
                LoginCallbackHandler handler = new LoginCallbackHandler(user.getName(), password);
                LoginContext c = new LoginContext(realm, handler);
                c.login();
            } catch (LoginException e) {
                return false;
            }
            return true;
        }

        @Override
        public boolean isSynthetic() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public String toString() {
            return getName() + "@" + realm;
        }

        @Override
        public boolean hasGroup(String group) {
            return groups.contains(group);
        }

        @Override
        public IReadProperties getAttributes() {
            return null;
        }

        @Override
        public void putAttributes(IReadProperties properties) throws NotSupportedException {
            throw new NotSupportedException();
        }

        @Override
        public String[] getGroups() throws NotSupportedException {
            return groups.toArray(new String[groups.size()]);
        }

        @Override
        public boolean reloadAccount() {
            groups.clear();
            for (GroupPrincipal grp : engine.listGroups(user))
                groups.add(grp.getName().trim().toLowerCase());
            return true;
        }

        @Override
        public Date getCreationDate() {
            return null;
        }

        @Override
        public Date getModifyDate() {
            return null;
        }

        @Override
        public UUID getUUID() {
            return null;
        }

        @Override
        public boolean isActive() {
            return true;
        }
    }

    @Override
    public ModifyAccountApi getModifyApi() {
        return null;
    }
}
