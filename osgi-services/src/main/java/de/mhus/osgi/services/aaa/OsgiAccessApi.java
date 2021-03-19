package de.mhus.osgi.services.aaa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.env.Environment;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.LifecycleUtils;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.lib.core.aaa.AccessApi;
import de.mhus.lib.core.aaa.DefaultAccessApi;
import de.mhus.lib.core.aaa.DummyRealm;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.api.aaa.RealmServiceProvider;
import de.mhus.osgi.api.util.MServiceTracker;

@Component(service = AccessApi.class, immediate = true)
public class OsgiAccessApi extends DefaultAccessApi {

    MServiceTracker<RealmServiceProvider> realmTracker = 
            new MServiceTracker<>(
                    RealmServiceProvider.class,
                    (reference, service) -> add(reference, service),
                    (reference, service) -> remove(reference, service)
                    );
    
    @Activate
    public void doActivate(ComponentContext ctx) {
        realmTracker.start(ctx);
    }

    @Deactivate
    public void doDeactivate() {
        realmTracker.stop();
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        loadServices(env.getSecurityManager());
    }

    @Override
    protected Environment createEnvironment() {
        return  new OsgiAccessEnvironment(CFG_CONFIG_FILE.value());
    }

    protected void loadServices(SecurityManager securityManager) {
        if (securityManager instanceof DefaultSecurityManager) {
            List<Realm> realms = new ArrayList<>();
            for (RealmServiceProvider service : MOsgi.getServices(RealmServiceProvider.class, null)) {
                log().i("add realm " + service.getService().getName() + " " + service.getService().getClass().getCanonicalName());
                realms.add(service.getService());
            }
            if (realms.size() > 0) {
                LifecycleUtils.init(realms);
                ((DefaultSecurityManager)securityManager).setRealms(realms);
            }
            
            ((DefaultSecurityManager)securityManager).setCacheManager( new OsgiCacheManager() );
        }
    }

    private void remove(ServiceReference<RealmServiceProvider> reference, RealmServiceProvider service) {
        SecurityManager manager = SecurityUtils.getSecurityManager();
        if (manager instanceof DefaultSecurityManager) {
            Collection<Realm> realms = ((DefaultSecurityManager)manager).getRealms();
            if (realms == null) return;
            Realm realm = service.getService();
            realms.remove(realm);
            LifecycleUtils.destroy(realm);
            if (realms.size() == 0)
                realms.add(new DummyRealm());
            ((DefaultSecurityManager)manager).setRealms(realms);
        } else
            log().d("SecurityManager is not DefaultSecurityManager",manager.getClass());
    }

    private void add(ServiceReference<RealmServiceProvider> reference, RealmServiceProvider service) {
        SecurityManager manager = SecurityUtils.getSecurityManager();
        if (manager instanceof DefaultSecurityManager) {
            Collection<Realm> realms = ((DefaultSecurityManager)manager).getRealms();
            if (realms == null) realms = new ArrayList<Realm>(1);
            Realm realm = service.getService();
            for (Realm r : realms)
                if (r.getName().equals(realm.getName()))
                    return; // already in list
            realms.add(realm);
            LifecycleUtils.init(realm);
            ((DefaultSecurityManager)manager).setRealms(realms);
        } else
            log().d("SecurityManager is not DefaultSecurityManager",manager.getClass());
    }

}
