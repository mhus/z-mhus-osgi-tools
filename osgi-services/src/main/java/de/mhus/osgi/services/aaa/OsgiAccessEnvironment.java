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
