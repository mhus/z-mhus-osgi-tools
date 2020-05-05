package de.mhus.lib.mutable;

import java.io.File;

import org.osgi.service.cm.ConfigurationAdmin;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MConstants;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.mapi.IApiInternal;
import de.mhus.lib.core.mapi.MCfgManager;
import de.mhus.osgi.api.services.MOsgi;

public class KarafCfgManager extends MCfgManager {

    public KarafCfgManager(IApiInternal internal) {
        super(internal);
    }

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
                    if (!pid.equals(MConstants.CFG_SYSTEM)) {
                        MApi.dirtyLogInfo("KarafCfgManager::Register PID",pid);
                        registerCfgProvider(new KarfConfigProvider(pid));
                    }
                }
            }
        }
        
        startInitiators();
    }

}
