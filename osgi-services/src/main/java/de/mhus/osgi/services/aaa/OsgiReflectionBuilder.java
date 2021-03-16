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
            log.i("An instance with name '{}' already exists.  " +
                    "Redefining this object as a new instance of type {}", name, value);
        }

        Object instance;//name with no property, assume right hand side of equals sign is the class name:
        try {
            Class<?> clazz = cl.loadClass(value);
            instance = clazz.getConstructor().newInstance();
            if (instance instanceof Nameable) {
                ((Nameable) instance).setName(name);
            }
        } catch (Exception e) {
            String msg = "Unable to instantiate class [" + value + "] for object named '" + name + "'.  " +
                    "Please ensure you've specified the fully qualified class name correctly.";
            throw new ConfigurationException(msg, e);
        }
        objects.put(name, instance);
    }

}
