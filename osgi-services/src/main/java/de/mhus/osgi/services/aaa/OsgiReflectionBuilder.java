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

import java.util.Map;

import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.config.ReflectionBuilder;
import org.apache.shiro.util.Nameable;

import de.mhus.lib.core.logging.Log;
import de.mhus.osgi.api.util.OsgiBundleClassLoader;

public class OsgiReflectionBuilder extends ReflectionBuilder {

    private OsgiBundleClassLoader cl = new OsgiBundleClassLoader();
    private static Log log = Log.getLog(OsgiReflectionBuilder.class);

    @Override
    protected void createNewInstance(Map<String, Object> objects, String name, String value) {

        Object currentInstance = objects.get(name);
        if (currentInstance != null) {
            log.i(
                    "An instance with name '{}' already exists.  "
                            + "Redefining this object as a new instance of type {}",
                    name,
                    value);
        }

        Object instance; // name with no property, assume right hand side of equals sign is the
        // class name:
        try {
            Class<?> clazz = cl.loadClass(value);
            instance = clazz.getConstructor().newInstance();
            if (instance instanceof Nameable) {
                ((Nameable) instance).setName(name);
            }
        } catch (Exception e) {
            String msg =
                    "Unable to instantiate class ["
                            + value
                            + "] for object named '"
                            + name
                            + "'.  "
                            + "Please ensure you've specified the fully qualified class name correctly.";
            throw new ConfigurationException(msg, e);
        }
        objects.put(name, instance);
    }
}
