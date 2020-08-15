package de.mhus.osgi.services.shiro;

import org.apache.shiro.config.Ini;
import org.apache.shiro.env.DefaultEnvironment;

public class MyBasicIniEnvironment extends DefaultEnvironment {

    public MyBasicIniEnvironment(Ini ini) {
        setSecurityManager(new MyIniSecurityManagerFactory(ini).getInstance());
    }

    public MyBasicIniEnvironment(String iniResourcePath) {
        this(Ini.fromResourcePath(iniResourcePath));
    }
}
