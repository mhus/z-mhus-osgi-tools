package de.mhus.osgi.api.cluster;

public interface LockListener {

    enum EVENT {
        LOCK,
        UNLOCK
    };

    void event(EVENT event, String lock, boolean local);
}
