package de.mhus.osgi.services.shiro;

import java.util.HashMap;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.env.DefaultEnvironment;
import org.osgi.service.component.annotations.Component;

import de.mhus.lib.core.shiro.AccessApi;
import de.mhus.lib.core.shiro.DefaultAccessApi;
import de.mhus.lib.core.shiro.EmptySecurityManager;

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
