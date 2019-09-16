/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.mhus;

import org.apache.karaf.shell.api.console.Session;

import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.osgi.api.karaf.CmdInterceptor;

public class LogInterceptor implements CmdInterceptor {

    private String cfg;

    public LogInterceptor(String cfg) {
        MLogUtil.setTrailConfig(MLogUtil.TRAIL_SOURCE_SHELL, cfg);
        this.cfg = MLogUtil.getTrailConfig();
        MLogUtil.releaseTrailConfig();
    }

    @Override
    public void onCmdStart(Session session) {
        MLogUtil.setTrailConfig(MLogUtil.TRAIL_SOURCE_SHELL, cfg);
    }

    @Override
    public void onCmdEnd(Session session) {
        MLogUtil.releaseTrailConfig();
    }

    public String getConfig() {
        return cfg;
    }
    
}
