package de.mhus.osgi.services.shiro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.RealmFactory;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.util.Nameable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyIniSecurityManagerFactory extends MyIniFactorySupport<SecurityManager> {

    public static final String MAIN_SECTION_NAME = "main";

    public static final String SECURITY_MANAGER_NAME = "securityManager";
    public static final String INI_REALM_NAME = "iniRealm";

    private static final transient Logger log =
            LoggerFactory.getLogger(MyIniSecurityManagerFactory.class);

    private MyReflectionBuilder builder;

    /**
     * Creates a new instance. See the {@link #getInstance()} JavaDoc for detailed explanation of
     * how an INI source will be resolved to use to build the instance.
     */
    public MyIniSecurityManagerFactory() {
        this.builder = new MyReflectionBuilder();
    }

    public MyIniSecurityManagerFactory(Ini config) {
        this();
        setIni(config);
    }

    public MyIniSecurityManagerFactory(String iniResourcePath) {
        this(Ini.fromResourcePath(iniResourcePath));
    }

    public Map<String, ?> getBeans() {
        return this.builder != null ? Collections.unmodifiableMap(builder.getObjects()) : null;
    }

    public void destroy() {
        if (getMyReflectionBuilder() != null) {
            getMyReflectionBuilder().destroy();
        }
    }

    private SecurityManager getSecurityManagerBean() {
        return getMyReflectionBuilder().getBean(SECURITY_MANAGER_NAME, SecurityManager.class);
    }

    @Override
    protected SecurityManager createDefaultInstance() {
        return new DefaultSecurityManager();
    }

    @Override
    protected SecurityManager createInstance(Ini ini) {
        if (CollectionUtils.isEmpty(ini)) {
            throw new NullPointerException("Ini argument cannot be null or empty.");
        }
        SecurityManager securityManager = createSecurityManager(ini);
        if (securityManager == null) {
            String msg = SecurityManager.class + " instance cannot be null.";
            throw new ConfigurationException(msg);
        }
        return securityManager;
    }

    private SecurityManager createSecurityManager(Ini ini) {
        return createSecurityManager(ini, getConfigSection(ini));
    }

    private Ini.Section getConfigSection(Ini ini) {

        Ini.Section mainSection = ini.getSection(MAIN_SECTION_NAME);
        if (CollectionUtils.isEmpty(mainSection)) {
            // try the default:
            mainSection = ini.getSection(Ini.DEFAULT_SECTION_NAME);
        }
        return mainSection;
    }

    protected boolean isAutoApplyRealms(SecurityManager securityManager) {
        boolean autoApply = true;
        if (securityManager instanceof RealmSecurityManager) {
            // only apply realms if they haven't been explicitly set by the user:
            RealmSecurityManager realmSecurityManager = (RealmSecurityManager) securityManager;
            Collection<Realm> realms = realmSecurityManager.getRealms();
            if (!CollectionUtils.isEmpty(realms)) {
                log.info(
                        "Realms have been explicitly set on the SecurityManager instance - auto-setting of "
                                + "realms will not occur.");
                autoApply = false;
            }
        }
        return autoApply;
    }

    private SecurityManager createSecurityManager(Ini ini, Ini.Section mainSection) {

        getMyReflectionBuilder().setObjects(createDefaults(ini, mainSection));
        Map<String, ?> objects = buildInstances(mainSection);

        SecurityManager securityManager = getSecurityManagerBean();

        boolean autoApplyRealms = isAutoApplyRealms(securityManager);

        if (autoApplyRealms) {
            // realms and realm factory might have been created - pull them out first so we can
            // initialize the securityManager:
            Collection<Realm> realms = getRealms(objects);
            // set them on the SecurityManager
            if (!CollectionUtils.isEmpty(realms)) {
                applyRealmsToSecurityManager(realms, securityManager);
            }
        }

        return securityManager;
    }

    protected Map<String, ?> createDefaults(Ini ini, Ini.Section mainSection) {
        Map<String, Object> defaults = new LinkedHashMap<String, Object>();

        SecurityManager securityManager = createDefaultInstance();
        defaults.put(SECURITY_MANAGER_NAME, securityManager);

        if (shouldImplicitlyCreateRealm(ini)) {
            Realm realm = createRealm(ini);
            if (realm != null) {
                defaults.put(INI_REALM_NAME, realm);
            }
        }

        // The values from 'getDefaults()' will override the above.
        Map<String, ?> defaultBeans = getDefaults();
        if (!CollectionUtils.isEmpty(defaultBeans)) {
            defaults.putAll(defaultBeans);
        }

        return defaults;
    }

    private Map<String, ?> buildInstances(Ini.Section section) {
        return getMyReflectionBuilder().buildObjects(section);
    }

    private void addToRealms(Collection<Realm> realms, RealmFactory factory) {
        LifecycleUtils.init(factory);
        Collection<Realm> factoryRealms = factory.getRealms();
        // SHIRO-238: check factoryRealms (was 'realms'):
        if (!CollectionUtils.isEmpty(factoryRealms)) {
            realms.addAll(factoryRealms);
        }
    }

    private Collection<Realm> getRealms(Map<String, ?> instances) {

        // realms and realm factory might have been created - pull them out first so we can
        // initialize the securityManager:
        List<Realm> realms = new ArrayList<Realm>();

        // iterate over the map entries to pull out the realm factory(s):
        for (Map.Entry<String, ?> entry : instances.entrySet()) {

            String name = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof RealmFactory) {
                addToRealms(realms, (RealmFactory) value);
            } else if (value instanceof Realm) {
                Realm realm = (Realm) value;
                // set the name if null:
                String existingName = realm.getName();
                if (existingName == null || existingName.startsWith(realm.getClass().getName())) {
                    if (realm instanceof Nameable) {
                        ((Nameable) realm).setName(name);
                        log.debug("Applied name '{}' to Nameable realm instance {}", name, realm);
                    } else {
                        log.info(
                                "Realm does not implement the {} interface.  Configured name will not be applied.",
                                Nameable.class.getName());
                    }
                }
                realms.add(realm);
            }
        }

        return realms;
    }

    private void assertRealmSecurityManager(SecurityManager securityManager) {
        if (securityManager == null) {
            throw new NullPointerException("securityManager instance cannot be null");
        }
        if (!(securityManager instanceof RealmSecurityManager)) {
            String msg =
                    "securityManager instance is not a "
                            + RealmSecurityManager.class.getName()
                            + " instance.  This is required to access or configure realms on the instance.";
            throw new ConfigurationException(msg);
        }
    }

    protected void applyRealmsToSecurityManager(
            Collection<Realm> realms, SecurityManager securityManager) {
        assertRealmSecurityManager(securityManager);
        ((RealmSecurityManager) securityManager).setRealms(realms);
    }

    /**
     * Returns {@code true} if the Ini contains account data and a {@code Realm} should be
     * implicitly {@link #createRealm(Ini) created} to reflect the account data, {@code false} if no
     * realm should be implicitly created.
     *
     * @param ini the Ini instance to inspect for account data resulting in an implicitly created
     *     realm.
     * @return {@code true} if the Ini contains account data and a {@code Realm} should be
     *     implicitly {@link #createRealm(Ini) created} to reflect the account data, {@code false}
     *     if no realm should be implicitly created.
     */
    protected boolean shouldImplicitlyCreateRealm(Ini ini) {
        return !CollectionUtils.isEmpty(ini)
                && (!CollectionUtils.isEmpty(ini.getSection(IniRealm.ROLES_SECTION_NAME))
                        || !CollectionUtils.isEmpty(ini.getSection(IniRealm.USERS_SECTION_NAME)));
    }

    /**
     * Creates a {@code Realm} from the Ini instance containing account data.
     *
     * @param ini the Ini instance from which to acquire the account data.
     * @return a new Realm instance reflecting the account data discovered in the {@code Ini}.
     */
    protected Realm createRealm(Ini ini) {
        // IniRealm realm = new IniRealm(ini); changed to support SHIRO-322
        IniRealm realm = new IniRealm();
        realm.setName(INI_REALM_NAME);
        realm.setIni(ini); // added for SHIRO-322
        return realm;
    }

    /**
     * Returns the MyReflectionBuilder instance used to create SecurityManagers object graph.
     *
     * @return MyReflectionBuilder instance used to create SecurityManagers object graph.
     * @since 1.4
     */
    public MyReflectionBuilder getMyReflectionBuilder() {
        return builder;
    }

    /**
     * Sets the MyReflectionBuilder that will be used to create the SecurityManager based on the
     * contents of the Ini configuration.
     *
     * @param builder The MyReflectionBuilder used to parse the Ini configuration.
     * @since 1.4
     */
    @SuppressWarnings("unused")
    public void setMyReflectionBuilder(MyReflectionBuilder builder) {
        this.builder = builder;
    }
}
