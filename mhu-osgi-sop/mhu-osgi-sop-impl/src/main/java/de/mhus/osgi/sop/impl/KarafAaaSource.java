package de.mhus.osgi.sop.impl;

import java.util.HashSet;
import java.util.List;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;
import org.apache.karaf.jaas.modules.BackingEngine;
import org.apache.karaf.jaas.modules.BackingEngineFactory;

import de.mhus.lib.core.security.LoginCallbackHandler;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.osgi.sop.api.aaa.Account;
import de.mhus.osgi.sop.api.aaa.AccountSource;

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
                    	 String moduleClass = (String) e.getOptions().get(ProxyLoginModule.PROPERTY_MODULE);
                    	 if (moduleName == null || e.getLoginModuleName().equals(moduleName) || moduleName.equals(moduleClass)) {
	                        engine = getBackingEngine(e);
	                        if (engine != null)
	                            break;
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
					return new KarafAccount(engine,user);
				}
			}
		} catch (Throwable t ) {
			t.printStackTrace();
		}
		return null;
	}

	public BackingEngine getBackingEngine(AppConfigurationEntry entry) {
        List<BackingEngineFactory> engineFactories = MOsgi.getServices(BackingEngineFactory.class, null);
        for (BackingEngineFactory factory : engineFactories) {
            String loginModuleClass = (String) entry.getOptions().get(ProxyLoginModule.PROPERTY_MODULE);
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
			for (GroupPrincipal grp : engine.listGroups(user))
				groups.add(grp.getName().trim().toLowerCase());
			
		}

		@Override
		public String getAccount() {
			return user.getName();
		}

		@Override
		public boolean isValide() {
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
		public boolean isSyntetic() {
			return false;
		}

		@Override
		public String getDisplayName() {
			return getAccount();
		}
		
		public String toString() {
			return getAccount() + "@" + realm;
		}

		@Override
		public boolean hasGroup(String group) {
			return groups.contains(group);
		}
		
	}
	
}
