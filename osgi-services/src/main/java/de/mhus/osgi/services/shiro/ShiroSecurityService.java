package de.mhus.osgi.services.shiro;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.shiro.SecurityUtils;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.shiro.DefaultShiroSecurity;
import de.mhus.lib.core.shiro.ShiroSecurity;

@Component(service = ShiroSecurity.class)
public class ShiroSecurityService extends DefaultShiroSecurity {

    @Override
    protected void initialize() {
        new ConvertUtilsBean();
        log().d("Initialize Shiro",CFG_CONFIG_FILE);
        env = new MyBasicIniEnvironment(CFG_CONFIG_FILE.value());
        SecurityUtils.setSecurityManager(env.getSecurityManager());
//        Factory<SecurityManager> factory = new IniSecurityManagerFactory(CFG_CONFIG_FILE.value());
//        securityManager = factory.getInstance();
//        SecurityUtils.setSecurityManager(securityManager);
    }

}
