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
package de.mhus.lib.mutable;

import java.io.IOException;
import java.util.Enumeration;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import de.mhus.lib.core.cfg.CfgProvider;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.config.MConfig;
import de.mhus.osgi.api.MOsgi;

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
    public void doStop() {}
}
