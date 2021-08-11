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
package de.mhus.osgi.services.aaa;

import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.env.DefaultEnvironment;

@SuppressWarnings("deprecation")
public class OsgiAccessEnvironment extends DefaultEnvironment {

    @SuppressWarnings("unused")
    private Ini ini;

    public OsgiAccessEnvironment(String resourcePath) {
        this(Ini.fromResourcePath(resourcePath));
    }

    public OsgiAccessEnvironment(Ini ini) {
        super(ini);
        this.ini = ini;
        IniSecurityManagerFactory factory = new IniSecurityManagerFactory(ini);

        factory.setReflectionBuilder(new OsgiReflectionBuilder());

        setSecurityManager(factory.getInstance());
    }

    public void removeObject(String name) {
        objects.remove(name);
    }
}
