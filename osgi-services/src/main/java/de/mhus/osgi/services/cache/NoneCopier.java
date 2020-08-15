package de.mhus.osgi.services.cache;

import org.ehcache.impl.copy.ReadWriteCopier;
import org.ehcache.spi.copy.Copier;

public class NoneCopier<T> extends ReadWriteCopier<T> implements Copier<T> {

    @Override
    public T copy(T obj) {
        return obj;
    }
}
