package de.mhus.osgi.api.aaa;

import org.apache.shiro.realm.Realm;

public interface RealmServiceProvider {

    Realm getService();

}
