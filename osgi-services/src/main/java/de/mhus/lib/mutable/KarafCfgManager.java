package de.mhus.lib.mutable;

import java.io.File;

import org.osgi.service.cm.ConfigurationAdmin;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.mapi.MCfgManager;
import de.mhus.osgi.api.MOsgi;

public class KarafCfgManager extends MCfgManager {

    @Override
    protected void initialConfiguration() {
        CentralMhusCfgProvider provider = new CentralMhusCfgProvider();
        registerCfgProvider(provider);

        // load all from etc
        ConfigurationAdmin admin = MOsgi.getServiceOrNull(ConfigurationAdmin.class);
        if (admin == null) {
            MApi.dirtyLogError("ConfigurationAdmin is null");
        } else {
            for (File file : new File("etc").listFiles()) {
                if (file.isFile() && file.getName().endsWith(".cfg")) {
                    String pid = MString.beforeLastIndex(file.getName(), '.');
                    update(pid);
                }
            }
        }

        // prepare system config for default
        IConfig system = provider.getConfig();
        if (!system.containsKey(MConstants.PROP_LOG_FACTORY_CLASS)) {
            system.setString(MConstants.PROP_LOG_FACTORY_CLASS, "de.mhus.lib.logging.Log4JFactory");
        }
        if (!system.containsKey(MConstants.PROP_LOG_CONSOLE_REDIRECT)) {
            system.setString(MConstants.PROP_LOG_CONSOLE_REDIRECT, "false");
        }
    }

    public void update(String pid) {
        if (!pid.equals(MConstants.CFG_SYSTEM)) {
            MApi.dirtyLogInfo("KarafCfgManager::Register PID", pid);
            registerCfgProvider(new KarfConfigProvider(pid));
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void reload(Object owner) {
        if (owner == null) return;

        if (owner instanceof String) {
            update(owner.toString());
        } else {
            Class clazz = owner.getClass();
            if (owner instanceof Class) clazz = (Class) owner;
            update(MOsgi.findServicePid(clazz));
        }
    }
}
