package de.mhus.lib.mutable;

import java.io.IOException;
import java.util.Enumeration;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import de.mhus.lib.core.cfg.CfgProvider;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.osgi.api.services.MOsgi;

public class KarfConfigProvider extends CfgProvider {

    private MConfig config;

    public KarfConfigProvider(String pid) {
        super(pid);
    }

    public void load() throws IOException {
        ConfigurationAdmin admin = MOsgi.getServiceOrNull(ConfigurationAdmin.class);
        Configuration configuration = admin.getConfiguration(getName());
        config = new MConfig();
        if (configuration != null && configuration.getProperties() != null) {
            Enumeration<String> enu = configuration.getProperties().keys();
            while (enu.hasMoreElements()) {
                String key = enu.nextElement();
                config.put(key, configuration.getProperties().get(key));
            }
        }
    }
    
    @Override
    public IConfig getConfig() {
        return config;
    }

    @Override
    public void doRestart() {
        doStop();
        doStart();
    }

    @Override
    public void doStart() {
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doStop() {

    }

}
