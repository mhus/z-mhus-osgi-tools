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
package de.mhus.osgi.services.shiro;

import java.util.HashMap;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.env.DefaultEnvironment;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.aaa.AccessApi;
import de.mhus.lib.core.aaa.DefaultAccessApi;
import de.mhus.lib.core.aaa.EmptySecurityManager;

@Component(service = AccessApi.class)
public class AccessApiService extends DefaultAccessApi {

    @Override
    protected void initialize() {
        new ConvertUtilsBean();
        log().d("Initialize Shiro", CFG_CONFIG_FILE);
        try {
            env = new MyBasicIniEnvironment(CFG_CONFIG_FILE.value());
        } catch (Exception e) {
            log().d("Initialize empty shiro", CFG_CONFIG_FILE, e.toString());
            HashMap<String, Object> seed = new HashMap<>();
            seed.put(DefaultEnvironment.DEFAULT_SECURITY_MANAGER_KEY, new EmptySecurityManager());
            env = new DefaultEnvironment(seed);
        }
        SecurityUtils.setSecurityManager(env.getSecurityManager());
        //        Factory<SecurityManager> factory = new
        // IniSecurityManagerFactory(CFG_CONFIG_FILE.value());
        //        securityManager = factory.getInstance();
        //        SecurityUtils.setSecurityManager(securityManager);
    }
}
